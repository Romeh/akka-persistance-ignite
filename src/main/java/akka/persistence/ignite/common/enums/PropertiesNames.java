package akka.persistence.ignite.common.enums;

import lombok.Getter;

/**
 * the ignite properties names
 */
public enum PropertiesNames {
    IS_CLIENT_NODE("ignite.isClientNode"),
    TCP_DISCOVERY_ADDRESSES("ignite.tcpDiscoveryAddresses"),
    METRICS_LOG_FREQUENCY("ignite.metricsLogFrequency"),
    QUERY_THREADS("ignite.queryThreadPoolSize"),
    DATA_STREAMER_THREADS("ignite.dataStreamerThreadPoolSize"),
    MANAGEMENT_THREADS("ignite.managementThreadPoolSize"),
    PUBLIC_THREADS("ignite.publicThreadPoolSize"),
    SYSTEM_THREADS("ignite.systemThreadPoolSize"),
    REBALANCE_THREADS("ignite.rebalanceThreadPoolSize"),
    ASYNC_THREADS("ignite.asyncCallbackPoolSize"),
    PEER_CLASS_LOADING("ignite.peerClassLoadingEnabled"),
    ENABLE_FILE_PERSISTANCE("ignite.enableFilePersistence"),
    IGNITE_CONNECTOR_PORT("ignite.igniteConnectorPort"),
    IGNITE_SERVER_PORT_RANGE("ignite.igniteServerPortRange"),
    IGNITE_PERSISTANCE_FILE_PATH("ignite.ignitePersistenceFilePath"),
    CACHE_PREFIX_PROPERTY ("cache-prefix"),
    CACHE_BACKUPS ("cache-backups"),
    CACHE_CREATED_ALREADY ("cachesAlreadyCreated"),
    SEQUENCE_CACHE_NAME ("sequenceNumberTrack");

    @Getter
    private final String propertyName;

    PropertiesNames(String propertyName) {
        this.propertyName = propertyName;
    }
}
