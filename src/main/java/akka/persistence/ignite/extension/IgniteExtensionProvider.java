package akka.persistence.ignite.extension;

import akka.actor.*;
import akka.persistence.ignite.extension.impl.IgniteFactoryByConfig;
import lombok.*;
import org.apache.ignite.Ignite;
import scala.concurrent.ExecutionContextExecutor;

import java.util.function.Function;

/**
 * the provider for ignite akka extension to be used in journal and snapshot operations
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IgniteExtensionProvider extends AbstractExtensionId<IgniteExtension> implements ExtensionIdProvider {

    public static final IgniteExtensionProvider EXTENSION = new IgniteExtensionProvider();
    @Setter
    private Function<ExtendedActorSystem, Ignite> factory = new IgniteFactoryByConfig();

    /**
     * @return the ignite extension
     */
    @Override
    public ExtensionId<? extends Extension> lookup() {
        return EXTENSION;
    }

    /**
     * @param system akka actor system
     * @return the created ignite extension
     */
    @Override
    public IgniteExtension createExtension(ExtendedActorSystem system) {
        return new SimpleIgniteExtension(system.dispatcher(), factory.apply(system));
    }

    @AllArgsConstructor
    @Getter
    private class SimpleIgniteExtension implements IgniteExtension {
        private ExecutionContextExecutor dispatcher;
        private Ignite ignite;
    }
}
