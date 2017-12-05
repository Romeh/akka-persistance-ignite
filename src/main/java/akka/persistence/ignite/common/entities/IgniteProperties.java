package akka.persistence.ignite.common.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * the ignite properties pojo object
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class IgniteProperties {

    private boolean isClientNode = false;
    // for ONLY testing
    private String tcpDiscoveryAddresses = "localhost";
    private int metricsLogFrequency = 0;
    // thread pools based into target machine specs
    private int queryThreadPoolSize = 4;
    private int dataStreamerThreadPoolSize = 1;
    private int managementThreadPoolSize = 2;
    private int publicThreadPoolSize = 4;
    private int systemThreadPoolSize = 2;
    private int rebalanceThreadPoolSize = 1;
    private int asyncCallbackPoolSize = 4;
    private boolean peerClassLoadingEnabled = false;
    private boolean enableFilePersistence = true;
    private int igniteConnectorPort = 11211;
    private String igniteServerPortRange = "47500..47509";
    private String ignitePersistenceFilePath = "./data";
}
