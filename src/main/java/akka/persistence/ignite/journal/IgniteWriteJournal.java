package akka.persistence.ignite.journal;


import java.io.NotSerializableException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.cache.Cache;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cache.query.SqlQuery;

import com.typesafe.config.Config;

import akka.actor.ActorSystem;
import akka.dispatch.Futures;
import akka.persistence.AtomicWrite;
import akka.persistence.PersistentRepr;
import akka.persistence.ignite.common.JournalCacheProvider;
import akka.persistence.ignite.common.entities.JournalCaches;
import akka.persistence.ignite.extension.Store;
import akka.persistence.journal.japi.AsyncWriteJournal;
import akka.serialization.SerializationExtension;
import akka.serialization.Serializer;
import lombok.extern.slf4j.Slf4j;
import scala.collection.JavaConverters;
import scala.concurrent.Future;

/**
 * Created by MRomeh
 * the main ignite journal plugin implementation based into AsyncWriteJournal
 */
@Slf4j
public class IgniteWriteJournal extends AsyncWriteJournal {

    private final Serializer serializer;
    private final Store<JournalItem> storage;
    private final IgniteCache<Long, JournalItem> cache;
    private final IgniteCache<String, Long> sequenceNumberTrack;
    private final BiFunction<Config, ActorSystem, JournalCaches> journalCacheProvider = new JournalCacheProvider();

    /**
     * @param config akka configuration
     * @throws NotSerializableException
     */
    public IgniteWriteJournal(Config config) throws NotSerializableException {
        ActorSystem actorSystem = context().system();
        serializer = SerializationExtension.get(actorSystem).serializerFor(PersistentRepr.class);
        storage = new Store<>(actorSystem);
        JournalCaches journalCaches = journalCacheProvider.apply(config, actorSystem);
        sequenceNumberTrack = journalCaches.getSequenceCache();
        cache = journalCaches.getJournalCache();
    }

    private static Stream<Long> listsToStreamLong(List<List<?>> list) {
        return list.stream().flatMap(Collection::stream).filter(o -> o instanceof Long).map(o -> (Long) o);
    }

    @Override
    public Future<Void> doAsyncReplayMessages(String persistenceId, long fromSequenceNr, long toSequenceNr, long max, Consumer<PersistentRepr> replayCallback) {

        return storage.execute(persistenceId, cache, (entityIdParam, cacheParam) -> {
            if (log.isDebugEnabled()) {
                log.debug("doAsyncReplayMessages with params persistenceId: '{}' :fromSequenceNr {} :toSequenceNr {} :max {}"
                        , persistenceId, fromSequenceNr, toSequenceNr, max);
            }

            try (QueryCursor<Cache.Entry<Long, JournalItem>> query = cache
                    .query(new SqlQuery<Long, JournalItem>(JournalItem.class, "sequenceNr >= ? AND sequenceNr <= ? AND persistenceId=?")
                            .setArgs(fromSequenceNr, toSequenceNr, persistenceId))) {
                final List<Cache.Entry<Long, JournalItem>> all = query.getAll();
                if (log.isDebugEnabled()) {
                    log.debug("replyMessage results {} {} {}", query.toString(), all.toString(), all.size());
                }

                if (null != all && !all.isEmpty()) {

                    if (all.size() < max) {
                        for (Cache.Entry<Long, JournalItem> entry : all) {
                            if (log.isDebugEnabled()) {
                                log.debug("replay message persistenceId '{}' getKey {}", persistenceId, entry.getKey());
                            }
                            replayCallback.accept(convert(entry.getValue()));
                        }
                    } else {
                        all.subList(0, (int) max).forEach(longJournalItemEntry -> {
                            if (log.isDebugEnabled()) {
                                log.debug("replay message persistenceId'{}' getKey {}", persistenceId, longJournalItemEntry.getKey());
                            }
                            replayCallback.accept(convert(longJournalItemEntry.getValue()));

                        });
                    }
                }
            }
            return null;
        });
    }

    @Override
    public Future<Long> doAsyncReadHighestSequenceNr(String persistenceId, long fromSequenceNr) {
        return Futures.future(() -> {
            if (log.isDebugEnabled()) {
                log.debug("doAsyncReadHighestSequenceNr '{}' - {}", persistenceId, fromSequenceNr);
            }
            if (sequenceNumberTrack.containsKey(persistenceId)) {
                long highestSequenceNr = sequenceNumberTrack.get(persistenceId);
                if (highestSequenceNr != 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("doAsyncReadHighestSequenceNr '{}' {} -> {}", persistenceId, fromSequenceNr, highestSequenceNr);
                    }
                    return highestSequenceNr;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("doAsyncReadHighestSequenceNr '{}' {} -> {}", persistenceId, fromSequenceNr, fromSequenceNr);
                    }
                    return fromSequenceNr;
                }
            }
            return fromSequenceNr;
        }, storage.getDispatcher());
    }

    @Override
    public Future<Iterable<Optional<Exception>>> doAsyncWriteMessages(Iterable<AtomicWrite> messages) {
        return Futures.sequence(
                StreamSupport.stream(messages.spliterator(), false)
                        .map(this::writeBatch)
                        .collect(Collectors.toList()), storage.getDispatcher()
        );
    }

    private Future<Optional<Exception>> writeBatch(AtomicWrite atomicWrite) {
        return storage.execute(atomicWrite.persistenceId(), cache, (entityIdParam, cacheParam) -> {
            try {
                Map<Long, JournalItem> batch = JavaConverters
                        .seqAsJavaListConverter(atomicWrite.payload())
                        .asJava().stream()
                        .map(this::convert)
                        .collect(Collectors.toMap(JournalItem::getSequenceNr, item -> item));

                cache.putAll(batch);

                if (log.isDebugEnabled()) {
                    log.debug("doAsyncWriteMessages persistenceId'{}': batch {}", atomicWrite.persistenceId(), batch);
                }
                return Optional.empty();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Optional.of(e);
            }
        });
    }

    @Override
    public Future<Void> doAsyncDeleteMessagesTo(String persistenceId, long toSequenceNr) {
        return storage.execute(persistenceId, cache, (entityIdParam, cacheParam) -> {
            if (log.isDebugEnabled()) {
                log.debug("doAsyncDeleteMessagesTo persistenceId'{}' toSequenceNr : {}", persistenceId, toSequenceNr);
            }

            List<List<?>> seq = cache
                    .query(new SqlFieldsQuery("select sequenceNr from JournalItem where sequenceNr <= ? and persistenceId=?")
                            .setArgs(toSequenceNr, persistenceId))
                    .getAll();
            Set<Long> keys = listsToStreamLong(seq).collect(Collectors.toSet());

            if (log.isDebugEnabled()) {
                log.debug("remove keys {}", keys);
            }

            cache.removeAll(keys);
            return null;
        });
    }

    private JournalItem convert(PersistentRepr p) {
        return new JournalItem(p.sequenceNr(), p.persistenceId(), serializer.toBinary(p));
    }

    private PersistentRepr convert(JournalItem item) {
        return (PersistentRepr) serializer.fromBinary(item.getPayload());
    }


}
