package akka.persistence.ignite.snapshot;

import static akka.persistence.ignite.common.enums.FieldNames.payload;
import static akka.persistence.ignite.common.enums.FieldNames.sequenceNr;
import static akka.persistence.ignite.common.enums.FieldNames.timestamp;

import java.io.NotSerializableException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.cache.Cache;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cache.query.SqlQuery;
import org.jetbrains.annotations.Nullable;

import com.typesafe.config.Config;

import akka.actor.ActorSystem;
import akka.persistence.SelectedSnapshot;
import akka.persistence.SnapshotMetadata;
import akka.persistence.SnapshotSelectionCriteria;
import akka.persistence.ignite.common.SnapshotCacheProvider;
import akka.persistence.ignite.common.entities.SnapshotlStarted;
import akka.persistence.ignite.extension.Store;
import akka.persistence.serialization.Snapshot;
import akka.persistence.snapshot.japi.SnapshotStore;
import akka.serialization.SerializationExtension;
import akka.serialization.Serializer;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.Future;

/**
 * Created by MRomeh
 * the main ignite snapshot implementation based into SnapshotStore
 */
@Slf4j
public class IgniteSnapshotStore extends SnapshotStore {

	private final Serializer serializer;
	private final Store<SnapshotItem> storage;
	private final IgniteCache<Long, SnapshotItem> cache;
	private final BiFunction<Config, ActorSystem, IgniteCache<Long, SnapshotItem>> snapshotCacheProvider =
			new SnapshotCacheProvider();

	public IgniteSnapshotStore(Config config) throws NotSerializableException {
		ActorSystem actorSystem = context().system();
		storage = new Store<>(actorSystem);
		serializer = SerializationExtension.get(actorSystem).serializerFor(Snapshot.class);
		cache = snapshotCacheProvider.apply(config, actorSystem);
		actorSystem.eventStream().publish(new SnapshotlStarted());
	}

	private static Set<Long> listsToSetLong(List<List<?>> list) {
		return list.stream().flatMap(Collection::stream).filter(o -> o instanceof Long).map(o -> (Long) o).collect(Collectors.toSet());
	}

	@Override
	public Future<Optional<SelectedSnapshot>> doLoadAsync(String persistenceId, SnapshotSelectionCriteria criteria) {
		return storage.execute(persistenceId, cache, (entityIdParam, cacheParam) -> doLoadAsyncLogic(persistenceId, criteria));
	}

	private Optional<SelectedSnapshot> doLoadAsyncLogic(String persistenceId, SnapshotSelectionCriteria criteria) {
		if (log.isDebugEnabled()) {
			log.debug("doLoadAsync '{}' {} {}", persistenceId, criteria.minSequenceNr(), criteria.toString());
		}
		final IgniteCache<Long, BinaryObject> snapShotCache = cache.withKeepBinary();
		try (QueryCursor<Cache.Entry<Long, BinaryObject>> query = snapShotCache
				.query(new SqlQuery<Long, BinaryObject>(SnapshotItem.class, "sequenceNr >= ? AND sequenceNr <= ? AND timestamp >= ? AND timestamp <= ? and persistenceId=?")
						.setArgs(criteria.minSequenceNr(), criteria.maxSequenceNr(), criteria.minTimestamp(), criteria.maxTimestamp(), persistenceId))) {

			List<Cache.Entry<Long, BinaryObject>> iterator = query.getAll();
			final Optional<Cache.Entry<Long, BinaryObject>> max = iterator.stream().max((o1, o2) -> {
				if (o1.getValue().<Long>field(sequenceNr.name()) > o2.getValue().<Long>field(sequenceNr.name())) {
					return 1;
				} else if (o1.getValue().<Long>field(timestamp.name()) > o2.getValue().<Long>field(timestamp.name())) {
					return 1;
				} else {
					return -1;
				}
			});
			return max.map(longSnapshotItemEntry -> convert(persistenceId, longSnapshotItemEntry.getValue()));
		}
	}

	@Override
	public Future<Void> doSaveAsync(SnapshotMetadata metadata, Object snapshot) {
		return storage.execute(metadata.persistenceId(), cache, (entityIdParam, cacheParam) -> {
			if (log.isDebugEnabled()) {
				log.debug("doSaveAsync '{}' ({})", metadata.persistenceId(), metadata.sequenceNr());
			}
			SnapshotItem item = convert(metadata, snapshot);
			cache.put(item.getSequenceNr(), item);
			return null;
		});
	}

	@Override
	public Future<Void> doDeleteAsync(SnapshotMetadata metadata) {
		return storage.execute(metadata.persistenceId(), cache, (entityIdParam, cacheParam) -> {
			if (log.isDebugEnabled()) {
				log.debug("doDeleteAsync '{}' ({})", metadata.persistenceId(), metadata.sequenceNr());
			}
			cache.remove(metadata.sequenceNr());
			return null;
		});
	}

	@Override
	public Future<Void> doDeleteAsync(String persistenceId, SnapshotSelectionCriteria criteria) {
		return storage.execute(persistenceId, cache, (entityIdParam, cacheParam) -> asyncDelete(persistenceId, criteria));
	}

	@Nullable
	private Void asyncDelete(String persistenceId, SnapshotSelectionCriteria criteria) {
		if (log.isDebugEnabled()) {
			log.debug("doDeleteAsync '{}' ({}; {})", persistenceId, criteria.minSequenceNr(), criteria.maxSequenceNr());
		}
		List<List<?>> seq = cache
				.query(new SqlFieldsQuery("select sequenceNr from SnapshotItem where sequenceNr >= ? AND sequenceNr <= ? AND timestamp >= ? AND timestamp <= ? and persistenceId=?")
						.setArgs(criteria.minSequenceNr(), criteria.maxSequenceNr(), criteria.minTimestamp(), criteria.maxTimestamp(), persistenceId))
				.getAll();
		Set<Long> keys = listsToSetLong(seq);

		if (log.isDebugEnabled()) {
			log.debug("remove keys {}", keys);
		}
		cache.removeAll(keys);
		return null;
	}

	private SnapshotItem convert(SnapshotMetadata metadata, Object snapshot) {
		return new SnapshotItem(metadata.sequenceNr(), metadata.persistenceId(), metadata.timestamp(), serializer.toBinary(new Snapshot(snapshot)));
	}

	private SelectedSnapshot convert(String persistenceId, BinaryObject item) {
		SnapshotMetadata metadata = new SnapshotMetadata(persistenceId, item.<Long>field(sequenceNr.name()), item.<Long>field(timestamp.name()));
		Snapshot snapshot = (Snapshot) serializer.fromBinary(item.field(payload.name()));
		return SelectedSnapshot.create(metadata, snapshot.data());
	}

}
