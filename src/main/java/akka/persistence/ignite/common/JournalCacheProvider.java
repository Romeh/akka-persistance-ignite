package akka.persistence.ignite.common;

import akka.actor.ActorSystem;
import akka.persistence.ignite.common.entities.JournalCaches;
import akka.persistence.ignite.extension.IgniteExtension;
import akka.persistence.ignite.extension.IgniteExtensionProvider;
import akka.persistence.ignite.journal.JournalItem;
import akka.persistence.ignite.journal.JournalStoreInterceptor;
import com.typesafe.config.Config;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;

import java.util.function.BiFunction;

/**
 * Journal and sequence caches provider based into the provided ignite cache configuration
 */
public class JournalCacheProvider implements BiFunction<Config, ActorSystem, JournalCaches> {

    private static final String CACHE_PREFIX_PROPERTY = "cache-prefix";
    private static final String CACHE_BACKUPS = "cache-backups";

    /**
     * @param config      the akka configuration object
     * @param actorSystem the akk actor system
     * @return the created journal and sequence ignite caches†
     */
    @Override
    public JournalCaches apply(Config config, ActorSystem actorSystem) {
        final IgniteExtension extension = IgniteExtensionProvider.EXTENSION.get(actorSystem);
        final String cachePrefix = config.getString(CACHE_PREFIX_PROPERTY);
        final int cacheBackups = config.getInt(CACHE_BACKUPS);
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
        eventStore.setIndexedTypes(Long.class, JournalItem.class);
        eventStore.setIndexedTypes(String.class, JournalItem.class);
        eventStore.setInterceptor(new JournalStoreInterceptor());

        //sequence Number Tracking
        final CacheConfiguration<String, Long> squenceNumberTrack = new CacheConfiguration();
        squenceNumberTrack.setCopyOnRead(false);
        if (cacheBackups > 0) {
            squenceNumberTrack.setBackups(cacheBackups);
        } else {
            squenceNumberTrack.setBackups(1);
        }
        squenceNumberTrack.setAtomicityMode(CacheAtomicityMode.ATOMIC);
        squenceNumberTrack.setName("sequenceNumberTrack");
        squenceNumberTrack.setCacheMode(CacheMode.PARTITIONED);
        squenceNumberTrack.setReadFromBackup(true);
        return JournalCaches.builder()
                .journalCache(extension.getIgnite().getOrCreateCache(eventStore))
                .sequenceCache(extension.getIgnite().getOrCreateCache(squenceNumberTrack))
                .build();
    }
}
