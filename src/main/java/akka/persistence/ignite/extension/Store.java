package akka.persistence.ignite.extension;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import javax.cache.CacheException;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteClientDisconnectedException;

import akka.actor.ActorSystem;
import akka.dispatch.Futures;
import lombok.Getter;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;


/**
 * <p>
 * Main wrapper for ignite cache operations with client node reconnection tryout otherwise
 * if it ignite started as  server node no such exceptions will be thrown
 */
public class Store<V> {
    @Getter
    private final ExecutionContextExecutor dispatcher;

    public Store(ActorSystem actorSystem) {
        IgniteExtension extension = IgniteExtensionProvider.EXTENSION.get(actorSystem);
        dispatcher = extension.getDispatcher();
    }


    public <R> Future<R> execute(String key, IgniteCache<Long, V> cache, BiFunction<String, IgniteCache<Long, V>, R> function) {
        return Futures.future(() -> {
            R result = null;
            try {
                result = function.apply(key, cache);
                // in case it is client node , client reconnect with 300 milliseconds timeout is used to retry to connect again to the server nodes grid
            } catch (IgniteClientDisconnectedException e) {
                e.reconnectFuture().get(300, TimeUnit.MILLISECONDS);
                result = function.apply(key, cache);
            } catch (CacheException cacheException) {
                if (cacheException.getCause() instanceof IgniteClientDisconnectedException) {
                    ((IgniteClientDisconnectedException) cacheException.getCause()).reconnectFuture().get(300, TimeUnit.MILLISECONDS);
                    result = function.apply(key, cache);
                }
            }
            return result;
        }, dispatcher);

    }
}
