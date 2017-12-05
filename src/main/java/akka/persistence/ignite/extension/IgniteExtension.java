package akka.persistence.ignite.extension;

import akka.actor.Extension;
import org.apache.ignite.Ignite;
import scala.concurrent.ExecutionContextExecutor;

/**
 * the main Ignite Akka extension interface
 */
public interface IgniteExtension extends Extension {

    ExecutionContextExecutor getDispatcher();

    Ignite getIgnite();
}
