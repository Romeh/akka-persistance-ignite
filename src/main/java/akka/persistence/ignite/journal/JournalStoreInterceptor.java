package akka.persistence.ignite.journal;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheInterceptorAdapter;
import org.apache.ignite.resources.IgniteInstanceResource;

import javax.cache.Cache;

/**
 * journal cache interceptor to keep tracking the highest sequence of specific persistent entry every time there is an insert
 */
public class JournalStoreInterceptor extends CacheInterceptorAdapter<Long, JournalItem> {

    @IgniteInstanceResource
    private transient Ignite ignite;

    @Override
    public void onAfterPut(Cache.Entry<Long, JournalItem> entry) {
        IgniteCache<String, Long> sequenceNumberTrack = ignite.getOrCreateCache("sequenceNumberTrack");
        sequenceNumberTrack.invoke(entry.getValue().getPersistenceId(), (mutableEntry, objects) -> {
            if (mutableEntry.exists() && mutableEntry.getValue() != null) {
                // if it is less than the new sequence value , use it
                if (mutableEntry.getValue() < entry.getKey()) {
                    mutableEntry.setValue(entry.getKey());
                }
            } else {
                // if does not exist , just use it
                mutableEntry.setValue(entry.getKey());
            }
            // by api design nothing needed here
            return null;
        });
    }
}
