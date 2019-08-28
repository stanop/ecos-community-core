package ru.citeck.ecos.eureka;

import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.shared.transport.EurekaTransportConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class EurekaAlfClientConfig extends AbstractEurekaConfig implements EurekaClientConfig {

    private EurekaClientConfig defaultConfig = new DefaultEurekaClientConfig();

    public EurekaAlfClientConfig(Properties globalProperties) {
        super(globalProperties);
    }

    boolean isEurekaEnabled() {
        return getBoolParam("enabled", () -> true);
    }

    @Override
    public int getRegistryFetchIntervalSeconds() {
        return defaultConfig.getRegistryFetchIntervalSeconds();
    }

    @Override
    public int getInstanceInfoReplicationIntervalSeconds() {
        return defaultConfig.getInstanceInfoReplicationIntervalSeconds();
    }

    @Override
    public int getInitialInstanceInfoReplicationIntervalSeconds() {
        return defaultConfig.getInitialInstanceInfoReplicationIntervalSeconds();
    }

    @Override
    public int getEurekaServiceUrlPollIntervalSeconds() {
        return defaultConfig.getEurekaServiceUrlPollIntervalSeconds();
    }

    @Override
    public String getProxyHost() {
        return defaultConfig.getProxyHost();
    }

    @Override
    public String getProxyPort() {
        return defaultConfig.getProxyPort();
    }

    @Override
    public String getProxyUserName() {
        return defaultConfig.getProxyUserName();
    }

    @Override
    public String getProxyPassword() {
        return defaultConfig.getProxyPassword();
    }

    @Override
    public boolean shouldGZipContent() {
        return defaultConfig.shouldGZipContent();
    }

    @Override
    public int getEurekaServerReadTimeoutSeconds() {
        return defaultConfig.getEurekaServerReadTimeoutSeconds();
    }

    @Override
    public int getEurekaServerConnectTimeoutSeconds() {
        return defaultConfig.getEurekaServerConnectTimeoutSeconds();
    }

    @Override
    public String getBackupRegistryImpl() {
        return defaultConfig.getBackupRegistryImpl();
    }

    @Override
    public int getEurekaServerTotalConnections() {
        return defaultConfig.getEurekaServerTotalConnections();
    }

    @Override
    public int getEurekaServerTotalConnectionsPerHost() {
        return defaultConfig.getEurekaServerTotalConnectionsPerHost();
    }

    @Override
    public String getEurekaServerURLContext() {
        return defaultConfig.getEurekaServerURLContext();
    }

    @Override
    public String getEurekaServerPort() {
        return defaultConfig.getEurekaServerPort();
    }

    @Override
    public String getEurekaServerDNSName() {
        return defaultConfig.getEurekaServerDNSName();
    }

    @Override
    public boolean shouldUseDnsForFetchingServiceUrls() {
        return defaultConfig.shouldUseDnsForFetchingServiceUrls();
    }

    @Override
    public boolean shouldRegisterWithEureka() {
        return getBoolParam("registration.enabled", defaultConfig::shouldRegisterWithEureka);
    }

    @Override
    public boolean shouldPreferSameZoneEureka() {
        return defaultConfig.shouldPreferSameZoneEureka();
    }

    @Override
    public boolean allowRedirects() {
        return defaultConfig.allowRedirects();
    }

    @Override
    public boolean shouldLogDeltaDiff() {
        return defaultConfig.shouldLogDeltaDiff();
    }

    @Override
    public boolean shouldDisableDelta() {
        return defaultConfig.shouldDisableDelta();
    }

    @Override
    public String fetchRegistryForRemoteRegions() {
        return defaultConfig.fetchRegistryForRemoteRegions();
    }

    @Override
    public String getRegion() {
        return defaultConfig.getRegion();
    }

    @Override
    public String[] getAvailabilityZones(String region) {
        return defaultConfig.getAvailabilityZones(region);
    }

    @Override
    public List<String> getEurekaServerServiceUrls(String myZone) {

        String serviceUrls = System.getenv("EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE");

        if (StringUtils.isBlank(serviceUrls)) {
            serviceUrls = getStrParam("serviceUrl." + myZone, () ->
                    getStrParam("serviceUrl.default", () -> null)
            );
        }

        if (serviceUrls != null) {
            return Arrays.asList(serviceUrls.split(DefaultEurekaClientConfig.URL_SEPARATOR));
        } else {
            return defaultConfig.getEurekaServerServiceUrls(myZone);
        }
    }

    @Override
    public boolean shouldFilterOnlyUpInstances() {
        return defaultConfig.shouldFilterOnlyUpInstances();
    }

    @Override
    public int getEurekaConnectionIdleTimeoutSeconds() {
        return defaultConfig.getEurekaConnectionIdleTimeoutSeconds();
    }

    @Override
    public boolean shouldFetchRegistry() {
        return defaultConfig.shouldFetchRegistry();
    }

    @Override
    public String getRegistryRefreshSingleVipAddress() {
        return defaultConfig.getRegistryRefreshSingleVipAddress();
    }

    @Override
    public int getHeartbeatExecutorThreadPoolSize() {
        return defaultConfig.getHeartbeatExecutorThreadPoolSize();
    }

    @Override
    public int getHeartbeatExecutorExponentialBackOffBound() {
        return defaultConfig.getHeartbeatExecutorExponentialBackOffBound();
    }

    @Override
    public int getCacheRefreshExecutorThreadPoolSize() {
        return defaultConfig.getCacheRefreshExecutorThreadPoolSize();
    }

    @Override
    public int getCacheRefreshExecutorExponentialBackOffBound() {
        return defaultConfig.getCacheRefreshExecutorExponentialBackOffBound();
    }

    @Override
    public String getDollarReplacement() {
        return defaultConfig.getDollarReplacement();
    }

    @Override
    public String getEscapeCharReplacement() {
        return defaultConfig.getEscapeCharReplacement();
    }

    @Override
    public boolean shouldOnDemandUpdateStatusChange() {
        return defaultConfig.shouldOnDemandUpdateStatusChange();
    }

    @Override
    public String getEncoderName() {
        return defaultConfig.getEncoderName();
    }

    @Override
    public String getDecoderName() {
        return getStrParam("decoderName", defaultConfig::getDecoderName);
    }

    @Override
    public String getClientDataAccept() {
        return defaultConfig.getClientDataAccept();
    }

    @Override
    public String getExperimental(String name) {
        return defaultConfig.getExperimental(name);
    }

    @Override
    public EurekaTransportConfig getTransportConfig() {
        return defaultConfig.getTransportConfig();
    }
}
