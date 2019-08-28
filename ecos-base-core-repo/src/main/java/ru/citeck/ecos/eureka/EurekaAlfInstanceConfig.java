package ru.citeck.ecos.eureka;

import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.discovery.shared.Pair;
import org.alfresco.util.GUID;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EurekaAlfInstanceConfig extends AbstractEurekaConfig implements EurekaInstanceConfig {

    private static final Logger logger = LoggerFactory.getLogger(EurekaAlfInstanceConfig.class);

    private static final String ENV_PROP_PORT = "ECOS_EUREKA_INSTANCE_PORT";
    private static final String ENV_PROP_IP = "ECOS_EUREKA_INSTANCE_IP";
    private static final String ENV_PROP_HOST = "ECOS_EUREKA_INSTANCE_HOST";

    private static final Pair<String, String> HOST_INFO = getHostInfo();
    private static final DataCenterInfo DATA_CENTER_INFO = () -> DataCenterInfo.Name.MyOwn;

    private static final String HEALTH_URL = "/alfresco/service/citeck/ecos/eureka-status";

    private static final String UUID = GUID.generate();

    public EurekaAlfInstanceConfig(Properties globalProperties) {
        super(globalProperties);
    }

    @Override
    public String getInstanceId() {
        return getAppname() + ":" + UUID;
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
        String portFromEnv = System.getenv(ENV_PROP_PORT);
        if (portFromEnv != null) {
            try {
                return Integer.parseInt(portFromEnv);
            } catch (NumberFormatException e) {
                logger.warn("Incorrect port in " + ENV_PROP_PORT + " param. Value: " + portFromEnv);
            }
        }
        return getIntParam("port", () -> getGlobalIntParam("alfresco.port", () -> 8080));
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
        return getAppname();
    }

    @Override
    public String getSecureVirtualHostName() {
        return getAppname();
    }

    @Override
    public String getASGName() {
        return null;
    }

    @Override
    public String getHostName(boolean refresh) {
        String host = System.getenv(ENV_PROP_HOST);
        if (StringUtils.isBlank(host)) {
            host = getStrParam("host", () -> getGlobalStrParam("alfresco.host", () -> "localhost"));
            if ("localhost".equals(host) || "127.0.0.1".equals(host)) {
                host = HOST_INFO.second();
            }
        }
        return host;
    }

    @Override
    public Map<String, String> getMetadataMap() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("zone", "primary");
        metadata.put("records-base-url", "/alfresco/s/citeck/ecos/records/");
        return metadata;
    }

    @Override
    public DataCenterInfo getDataCenterInfo() {
        return DATA_CENTER_INFO;
    }

    @Override
    public String getIpAddress() {
        String envValue = System.getenv(ENV_PROP_IP);
        if (StringUtils.isNotEmpty(envValue)) {
            return envValue;
        }
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
