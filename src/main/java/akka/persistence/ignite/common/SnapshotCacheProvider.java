package akka.persistence.ignite.common;

import static akka.persistence.ignite.common.enums.PropertiesNames.IGNITE_DATA_NAME;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.function.BiFunction;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.cache.QueryIndex;
import org.apache.ignite.cache.QueryIndexType;
import org.apache.ignite.configuration.CacheConfiguration;

import com.typesafe.config.Config;

import akka.actor.ActorSystem;
import akka.persistence.ignite.common.enums.FieldNames;
import akka.persistence.ignite.common.enums.PropertiesNames;
import akka.persistence.ignite.extension.IgniteExtension;
import akka.persistence.ignite.extension.IgniteExtensionProvider;
import akka.persistence.ignite.snapshot.SnapshotItem;

/**
 * Snapshot cache provider based into the provided ignite properties
 */
public class SnapshotCacheProvider implements BiFunction<Config, ActorSystem, IgniteCache<Long, SnapshotItem>> {


	/**
	 * @param config      the akka configuration object
	 * @param actorSystem the akk actor system
	 * @return the created snapshot ignite cache
	 */
	@Override
	public IgniteCache<Long, SnapshotItem> apply(Config config, ActorSystem actorSystem) {
		final IgniteExtension extension = IgniteExtensionProvider.EXTENSION.get(actorSystem);
		final String cachePrefix = config.getString(PropertiesNames.CACHE_PREFIX_PROPERTY.getPropertyName());
		final int cacheBackups = config.getInt(PropertiesNames.CACHE_BACKUPS.getPropertyName());
		final boolean cachesExit = config.getBoolean(PropertiesNames.CACHE_CREATED_ALREADY.getPropertyName());
		// if caches are already created in case of ignite is deployed as a standalone data grid and will not be auto started by the plugin
		if (cachesExit) {
			return extension.getIgnite().cache(cachePrefix + "_SNAPSHOT");
		} else {
			final CacheConfiguration<Long, SnapshotItem> eventStore = new CacheConfiguration<>();
			eventStore.setCopyOnRead(false);
			if (cacheBackups > 0) {
				eventStore.setBackups(cacheBackups);
			} else {
				eventStore.setBackups(1);
			}
			eventStore.setAtomicityMode(CacheAtomicityMode.ATOMIC);
			eventStore.setName(cachePrefix + "_SNAPSHOT");
			eventStore.setCacheMode(CacheMode.PARTITIONED);
			eventStore.setReadFromBackup(true);
			eventStore.setQueryEntities(Collections.singletonList(createSnapshotBinaryQueryEntity()));
			eventStore.setDataRegionName(IGNITE_DATA_NAME.getPropertyName());
			return extension.getIgnite().getOrCreateCache(eventStore);
		}

	}


	private QueryEntity createSnapshotBinaryQueryEntity() {
		QueryEntity queryEntity = new QueryEntity();
		queryEntity.setValueType(SnapshotItem.class.getName());
		queryEntity.setKeyType(Long.class.getName());
		LinkedHashMap<String, String> fields = new LinkedHashMap<>();
		fields.put(FieldNames.sequenceNr.name(), Long.class.getName());
		fields.put(FieldNames.persistenceId.name(), String.class.getName());
		fields.put(FieldNames.timestamp.name(), Long.class.getName());
		queryEntity.setFields(fields);
		final QueryIndex sequenceNrIndex = new QueryIndex(FieldNames.sequenceNr.name(), QueryIndexType.SORTED, false);
		final QueryIndex persistenceIdIndex = new QueryIndex(FieldNames.persistenceId.name());
		final QueryIndex timestampIndex = new QueryIndex(FieldNames.timestamp.name(), QueryIndexType.SORTED, false);
		queryEntity.setIndexes(Arrays.asList(sequenceNrIndex, persistenceIdIndex, timestampIndex));
		return queryEntity;

	}
}
