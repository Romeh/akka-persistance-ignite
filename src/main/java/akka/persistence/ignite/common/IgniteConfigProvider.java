package akka.persistence.ignite.common;

import java.util.function.Function;

import org.springframework.util.StringUtils;

import com.typesafe.config.Config;

import akka.actor.ActorSystem;
import akka.persistence.ignite.common.entities.IgniteProperties;
import akka.persistence.ignite.common.enums.PropertiesNames;

/**
 * the ignite properties provider based into the configuration provided otherwise use the default values
 */
public class IgniteConfigProvider implements Function<ActorSystem, IgniteProperties> {

	private final IgniteProperties igniteProperties = new IgniteProperties();

	/**
	 * @param actorSystem akka actor system
	 * @return return the configured ignite properties for the plugin
	 */
	@Override
	public IgniteProperties apply(ActorSystem actorSystem) {

		final Config config = actorSystem.settings().config();

		setIgniteThreadPools(config);
		setIgniteGeneralSettings(config);
		return igniteProperties;
	}

	private void setIgniteGeneralSettings(Config config) {
		if (config.getInt(PropertiesNames.METRICS_LOG_FREQUENCY.getPropertyName()) > 0) {
			igniteProperties.setMetricsLogFrequency(config
					.getInt(PropertiesNames.METRICS_LOG_FREQUENCY.getPropertyName()));
		}
		if (config.getInt(PropertiesNames.IGNITE_CONNECTOR_PORT.getPropertyName()) != 0) {
			igniteProperties.setIgniteConnectorPort(config
					.getInt(PropertiesNames.IGNITE_CONNECTOR_PORT.getPropertyName()));
		}
		if (!StringUtils.isEmpty(config.getString(PropertiesNames.IGNITE_PERSISTANCE_FILE_PATH.getPropertyName()))) {
			igniteProperties.setIgnitePersistenceFilePath(config
					.getString(PropertiesNames.IGNITE_PERSISTANCE_FILE_PATH.getPropertyName()));
		}
		if (!StringUtils.isEmpty(config.getString(PropertiesNames.IGNITE_SERVER_PORT_RANGE.getPropertyName()))) {
			igniteProperties.setIgniteServerPortRange(config
					.getString(PropertiesNames.IGNITE_SERVER_PORT_RANGE.getPropertyName()));
		}
		if (!StringUtils.isEmpty(config.getString(PropertiesNames.TCP_DISCOVERY_ADDRESSES.getPropertyName()))) {
			igniteProperties.setTcpDiscoveryAddresses(config
					.getString(PropertiesNames.TCP_DISCOVERY_ADDRESSES.getPropertyName()));
		}
		if (config.getBoolean(PropertiesNames.IS_CLIENT_NODE.getPropertyName())) {
			igniteProperties.setClientNode(config.getBoolean(PropertiesNames.IS_CLIENT_NODE.getPropertyName()));
		}
		if (config.getBoolean(PropertiesNames.ENABLE_FILE_PERSISTANCE.getPropertyName())) {
			igniteProperties.setEnableFilePersistence(config
					.getBoolean(PropertiesNames.ENABLE_FILE_PERSISTANCE.getPropertyName()));
		}
		if (config.getBoolean(PropertiesNames.PEER_CLASS_LOADING.getPropertyName())) {
			igniteProperties.setPeerClassLoadingEnabled(config
					.getBoolean(PropertiesNames.PEER_CLASS_LOADING.getPropertyName()));
		}
	}

	private void setIgniteThreadPools(Config config) {
		if (config.getInt(PropertiesNames.ASYNC_THREADS.getPropertyName()) != 0) {
			igniteProperties.setAsyncCallbackPoolSize(config
					.getInt(PropertiesNames.ASYNC_THREADS.getPropertyName()));
		}
		if (config.getInt(PropertiesNames.DATA_STREAMER_THREADS.getPropertyName()) != 0) {
			igniteProperties.setDataStreamerThreadPoolSize(config
					.getInt(PropertiesNames.DATA_STREAMER_THREADS.getPropertyName()));
		}
		if (config.getInt(PropertiesNames.MANAGEMENT_THREADS.getPropertyName()) != 0) {
			igniteProperties.setManagementThreadPoolSize(config
					.getInt(PropertiesNames.MANAGEMENT_THREADS.getPropertyName()));
		}
		if (config.getInt(PropertiesNames.PUBLIC_THREADS.getPropertyName()) != 0) {
			igniteProperties.setPublicThreadPoolSize(config
					.getInt(PropertiesNames.PUBLIC_THREADS.getPropertyName()));
		}
		if (config.getInt(PropertiesNames.SYSTEM_THREADS.getPropertyName()) != 0) {
			igniteProperties.setSystemThreadPoolSize(config
					.getInt(PropertiesNames.SYSTEM_THREADS.getPropertyName()));
		}
		if (config.getInt(PropertiesNames.QUERY_THREADS.getPropertyName()) != 0) {
			igniteProperties.setQueryThreadPoolSize(config
					.getInt(PropertiesNames.QUERY_THREADS.getPropertyName()));
		}
		if (config.getInt(PropertiesNames.REBALANCE_THREADS.getPropertyName()) != 0) {
			igniteProperties.setRebalanceThreadPoolSize(config
					.getInt(PropertiesNames.REBALANCE_THREADS.getPropertyName()));
		}
	}


}
