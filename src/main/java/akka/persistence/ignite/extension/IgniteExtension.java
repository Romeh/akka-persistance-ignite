package akka.persistence.ignite.extension;

import org.apache.ignite.Ignite;

import akka.actor.Extension;
import scala.concurrent.ExecutionContextExecutor;

/**
 * the main Ignite Akka extension interface
 */
public interface IgniteExtension extends Extension {

    ExecutionContextExecutor getDispatcher();

    Ignite getIgnite();
}
