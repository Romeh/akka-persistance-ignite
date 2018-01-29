package akka.persistence.ignite.extension.impl;

import java.util.Arrays;
import java.util.function.Function;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.BinaryConfiguration;
import org.apache.ignite.configuration.ConnectorConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import akka.actor.ActorSystem;
import akka.actor.ExtendedActorSystem;
import akka.persistence.ignite.common.IgniteConfigProvider;
import akka.persistence.ignite.common.entities.IgniteProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * the main class to start and configure ignite node part of akk extension
 */
@Slf4j
public class IgniteFactoryByConfig implements Function<ExtendedActorSystem, Ignite> {

    private final Function<ActorSystem, IgniteProperties> igniteConfigProvider = new IgniteConfigProvider();

    @Override
    public Ignite apply(ExtendedActorSystem system) {
        final IgniteProperties properties = igniteConfigProvider.apply(system);
        final IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
        igniteConfiguration.setClientMode(properties.isClientNode());
        // durable file memory persistence
        if (properties.isEnableFilePersistence()) {
            DataStorageConfiguration persistentStoreConfiguration = new DataStorageConfiguration();
            persistentStoreConfiguration.setStoragePath(properties.getIgnitePersistenceFilePath() + "/data/store");
            persistentStoreConfiguration.setWalArchivePath(properties.getIgnitePersistenceFilePath() + "./data/walArchive");
            igniteConfiguration.setDataStorageConfiguration(persistentStoreConfiguration);
        }
        // connector configuration
        final ConnectorConfiguration connectorConfiguration = new ConnectorConfiguration();
        connectorConfiguration.setPort(properties.getIgniteConnectorPort());
        // common ignite configuration
        igniteConfiguration.setMetricsLogFrequency(properties.getMetricsLogFrequency());
        igniteConfiguration.setQueryThreadPoolSize(properties.getQueryThreadPoolSize());
        igniteConfiguration.setDataStreamerThreadPoolSize(properties.getDataStreamerThreadPoolSize());
        igniteConfiguration.setManagementThreadPoolSize(properties.getManagementThreadPoolSize());
        igniteConfiguration.setPublicThreadPoolSize(properties.getPublicThreadPoolSize());
        igniteConfiguration.setSystemThreadPoolSize(properties.getSystemThreadPoolSize());
        igniteConfiguration.setRebalanceThreadPoolSize(properties.getRebalanceThreadPoolSize());
        igniteConfiguration.setAsyncCallbackPoolSize(properties.getAsyncCallbackPoolSize());
        igniteConfiguration.setPeerClassLoadingEnabled(properties.isPeerClassLoadingEnabled());

        final BinaryConfiguration binaryConfiguration = new BinaryConfiguration();
        binaryConfiguration.setCompactFooter(false);
        igniteConfiguration.setBinaryConfiguration(binaryConfiguration);
        // cluster tcp configuration
        final TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
        final TcpDiscoveryVmIpFinder tcpDiscoveryVmIpFinder = new TcpDiscoveryVmIpFinder();
        // need to be changed when it come to real cluster configuration
        tcpDiscoveryVmIpFinder.setAddresses(Arrays.asList(properties.getTcpDiscoveryAddresses() + properties.getIgniteServerPortRange()));
        tcpDiscoverySpi.setIpFinder(tcpDiscoveryVmIpFinder);
        igniteConfiguration.setDiscoverySpi(new TcpDiscoverySpi());
        final Ignite ignite = Ignition.start(igniteConfiguration);
        Runtime.getRuntime().addShutdownHook(new Thread(ignite::close));
        return ignite;
    }
}
