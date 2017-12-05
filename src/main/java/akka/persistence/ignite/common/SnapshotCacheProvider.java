package akka.persistence.ignite.common;

import akka.actor.ActorSystem;
import akka.persistence.ignite.extension.IgniteExtension;
import akka.persistence.ignite.extension.IgniteExtensionProvider;
import akka.persistence.ignite.snapshot.SnapshotItem;
import com.typesafe.config.Config;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;

import java.util.function.BiFunction;

/**
 * Snapshot cache provider based into the provided ignite properties
 */
public class SnapshotCacheProvider implements BiFunction<Config, ActorSystem, IgniteCache<Long, SnapshotItem>> {
    private static final String CACHE_PREFIX_PROPERTY = "cache-prefix";
    private static final String CACHE_BACKUPS = "cache-backups";

    /**
     * @param config      the akka configuration object
     * @param actorSystem the akk actor system
     * @return the created snapshot ignite cache
     */
    @Override
    public IgniteCache<Long, SnapshotItem> apply(Config config, ActorSystem actorSystem) {
        final IgniteExtension extension = IgniteExtensionProvider.EXTENSION.get(actorSystem);
        final String cachePrefix = config.getString(CACHE_PREFIX_PROPERTY);
        final int cacheBackups = config.getInt(CACHE_BACKUPS);
        final CacheConfiguration<Long, SnapshotItem> eventStore = new CacheConfiguration();
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
        eventStore.setIndexedTypes(Long.class, SnapshotItem.class);
        eventStore.setIndexedTypes(String.class, SnapshotItem.class);
        return extension.getIgnite().getOrCreateCache(eventStore);
    }
}
