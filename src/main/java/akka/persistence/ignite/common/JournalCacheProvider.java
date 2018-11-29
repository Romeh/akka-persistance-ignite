package akka.persistence.ignite.common;

import java.util.function.BiFunction;

import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.cache.QueryIndex;
import org.apache.ignite.configuration.CacheConfiguration;

import com.typesafe.config.Config;

import akka.actor.ActorSystem;
import akka.persistence.ignite.common.entities.JournalCaches;
import akka.persistence.ignite.common.enums.FieldNames;
import akka.persistence.ignite.common.enums.PropertiesNames;
import akka.persistence.ignite.extension.IgniteExtension;
import akka.persistence.ignite.extension.IgniteExtensionProvider;
import akka.persistence.ignite.journal.JournalItem;
import akka.persistence.ignite.journal.JournalStoreInterceptor;

/**
 * Journal and sequence caches provider based into the provided ignite cache configuration
 */
public class JournalCacheProvider implements BiFunction<Config, ActorSystem, JournalCaches> {


	/**
	 * @param config      the akka configuration object
	 * @param actorSystem the akk actor system
	 * @return the created journal and sequence ignite caches†
	 */
	@Override
	public JournalCaches apply(Config config, ActorSystem actorSystem) {
		final IgniteExtension extension = IgniteExtensionProvider.EXTENSION.get(actorSystem);
		final String cachePrefix = config.getString(PropertiesNames.CACHE_PREFIX_PROPERTY.getPropertyName());
		final boolean cachesExit = config.getBoolean(PropertiesNames.CACHE_CREATED_ALREADY.getPropertyName());
		final int cacheBackups = config.getInt(PropertiesNames.CACHE_BACKUPS.getPropertyName());
		// if caches are already created in case of ignite is deployed as a standalone data grid and will not be auto started by the plugin
		if (cachesExit) {
			return JournalCaches.builder()
					.journalCache(extension.getIgnite().cache(cachePrefix))
					.sequenceCache(extension.getIgnite().cache(PropertiesNames.SEQUENCE_CACHE_NAME.getPropertyName()))
					.build();
		} else {
			// cache configuration
			final CacheConfiguration<Long, JournalItem> eventStore = new CacheConfiguration();
			eventStore.setCopyOnRead(false);
			if (cacheBackups > 0) {
				eventStore.setBackups(cacheBackups);
			} else {
				eventStore.setBackups(1);
			}
			eventStore.setAtomicityMode(CacheAtomicityMode.ATOMIC);
			eventStore.setName(cachePrefix);
			eventStore.setCacheMode(CacheMode.PARTITIONED);
			eventStore.setReadFromBackup(true);
			eventStore.setQueryEntities(Collections.singletonList(createJournalBinaryQueryEntity()));
			eventStore.setInterceptor(new JournalStoreInterceptor());

			//sequence Number Tracking
			final CacheConfiguration<String, Long> squenceNumberTrack = new CacheConfiguration<>();
			squenceNumberTrack.setCopyOnRead(false);
			if (cacheBackups > 0) {
				squenceNumberTrack.setBackups(cacheBackups);
			} else {
				squenceNumberTrack.setBackups(1);
			}
			squenceNumberTrack.setAtomicityMode(CacheAtomicityMode.ATOMIC);
			squenceNumberTrack.setName(PropertiesNames.SEQUENCE_CACHE_NAME.getPropertyName());
			squenceNumberTrack.setCacheMode(CacheMode.PARTITIONED);
			squenceNumberTrack.setReadFromBackup(true);
			return JournalCaches.builder()
					.journalCache(extension.getIgnite().getOrCreateCache(eventStore))
					.sequenceCache(extension.getIgnite().getOrCreateCache(squenceNumberTrack))
					.build();
		}

	}


	private QueryEntity createJournalBinaryQueryEntity() {
		QueryEntity queryEntity = new QueryEntity();
		queryEntity.setValueType(JournalItem.class.getName());
		queryEntity.setKeyType(Long.class.getName());
		LinkedHashMap<String, String> fields = new LinkedHashMap<>();
		fields.put(FieldNames.sequenceNr.name(), Long.class.getName());
		fields.put(FieldNames.persistenceId.name(), String.class.getName());
		queryEntity.setFields(fields);
		queryEntity.setIndexes(Arrays.asList(new QueryIndex(FieldNames.sequenceNr.name()),
				new QueryIndex(FieldNames.persistenceId.name())));
		return queryEntity;

	}


}
