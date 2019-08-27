package ru.citeck.ecos.eureka;

import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.discovery.shared.Pair;
import org.alfresco.util.GUID;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class EurekaAlfInstanceConfig extends AbstractEurekaConfig implements EurekaInstanceConfig {

    private static final Pair<String, String> HOST_INFO = getHostInfo();
    private static final DataCenterInfo DATA_CENTER_INFO = () -> DataCenterInfo.Name.MyOwn;

    private static final String HEALTH_URL = "/alfresco/service/citeck/ecos/eureka-status";

    public EurekaAlfInstanceConfig(Properties globalProperties) {
        super(globalProperties);
    }

    @Override
    public String getInstanceId() {
        return getAppname() + ":" + GUID.generate();
    }

    @Override
    public String getAppname() {
        return getStrParam("instance.appname", () -> "alfresco");
    }

    @Override
    public String getAppGroupName() {
        return "alfresco";
    }

    @Override
    public boolean isInstanceEnabledOnit() {
        return getBoolParam("traffic.enabled", () -> false);
    }

    @Override
    public int getNonSecurePort() {
        return getGlobalIntParam("alfresco.port", () -> getIntParam("port", () -> 8080));
    }

    @Override
    public int getSecurePort() {
        return getIntParam("securePort", () -> 8443);
    }

    @Override
    public boolean isNonSecurePortEnabled() {
        return getBoolParam("port.enabled", () -> true);
    }

    @Override
    public boolean getSecurePortEnabled() {
        return getBoolParam("securePort.enabled", () -> false);
    }

    @Override
    public int getLeaseRenewalIntervalInSeconds() {
        return 30;
    }

    @Override
    public int getLeaseExpirationDurationInSeconds() {
        return 90;
    }

    @Override
    public String getVirtualHostName() {
        return this.getHostName(false) + ":" + this.getNonSecurePort();
    }

    @Override
    public String getSecureVirtualHostName() {
        return this.getHostName(false) + ":" + this.getSecurePort();
    }

    @Override
    public String getASGName() {
        return null;
    }

    @Override
    public String getHostName(boolean refresh) {
        return getGlobalStrParam("alfresco.host", () -> getStrParam("host", () -> "localhost"));
    }

    @Override
    public Map<String, String> getMetadataMap() {
        return Collections.emptyMap();
    }

    @Override
    public DataCenterInfo getDataCenterInfo() {
        return DATA_CENTER_INFO;
    }

    @Override
    public String getIpAddress() {
        return HOST_INFO.first();
    }

    @Override
    public String getStatusPageUrlPath() {
        return HEALTH_URL;
    }

    @Override
    public String getStatusPageUrl() {
        return HEALTH_URL;
    }

    @Override
    public String getHomePageUrlPath() {
        return "/alfresco/";
    }

    @Override
    public String getHomePageUrl() {
        return "/alfresco/";
    }

    @Override
    public String getHealthCheckUrlPath() {
        return HEALTH_URL;
    }

    @Override
    public String getHealthCheckUrl() {
        return HEALTH_URL;
    }

    @Override
    public String getSecureHealthCheckUrl() {
        return HEALTH_URL;
    }

    @Override
    public String[] getDefaultAddressResolutionOrder() {
        String result = getStrParam("defaultAddressResolutionOrder", () -> null);
        return result == null ? new String[0] : result.split(",");
    }

    @Override
    public String getNamespace() {
        return "alfresco";
    }

    private static Pair<String, String> getHostInfo() {
        Pair<String, String> pair;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            pair = new Pair<>(localHost.getHostAddress(), localHost.getHostName());
        } catch (UnknownHostException var2) {
            //logger.error("Cannot get host info", var2);
            pair = new Pair<>("", "");
        }

        return pair;
    }
}
