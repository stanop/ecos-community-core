package ru.citeck.ecos.eureka;

import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.discovery.EurekaClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class EcosEurekaClient {

    private static final Logger logger = LoggerFactory.getLogger(EcosEurekaClient.class);

    private static final Long INFO_CACHE_AGE = TimeUnit.SECONDS.toMillis(30L);
    private static final String ERROR_MSG = "Cannot get an instance of '%s' service from eureka";

    private Map<String, ServerInfo> serversInfo = new ConcurrentHashMap<>();
    private InstanceInfo.InstanceStatus status = InstanceInfo.InstanceStatus.STARTING;

    @Autowired
    @Qualifier("global-properties")
    private Properties properties;

    @Getter(lazy = true) private final DiscoveryManager manager = initManager();
    @Getter(lazy = true) private final EurekaClient client = initClient();

    @PostConstruct
    public void init() {
        try {
            getClient();
        } catch (EurekaDisabled e) {
            logger.info("Eureka disabled");
        } catch (Exception e) {
            logger.error("Eureka client init failed", e);
        }
    }

    public InstanceInfo getInstanceInfo(String instanceName) {
        ServerInfo info = serversInfo.computeIfAbsent(instanceName, this::getServerInfo);
        if (System.currentTimeMillis() - info.resolvedTimeMs > INFO_CACHE_AGE) {
            serversInfo.remove(instanceName);
            return getInstanceInfo(instanceName);
        }
        return info.getInfo();
    }

    private ServerInfo getServerInfo(String serverName) {
        InstanceInfo info;
        try {
            info = getClient().getNextServerFromEureka(serverName, false);
        } catch (Exception e) {
            throw new RuntimeException(String.format(ERROR_MSG, serverName), e);
        }
        if (info == null) {
            throw new RuntimeException(String.format(ERROR_MSG, serverName));
        }
        return new ServerInfo(info, System.currentTimeMillis());
    }

    private DiscoveryManager initManager() {
        DiscoveryManager manager = DiscoveryManager.getInstance();

        EurekaInstanceConfig instanceConfig = new EurekaAlfInstanceConfig(properties);
        EurekaAlfClientConfig clientConfig = new EurekaAlfClientConfig(properties);

        if (!clientConfig.isEurekaEnabled()) {
            throw new EurekaDisabled();
        }

        logger.info("===================================");
        logger.info("Register in eureka with params:");
        logger.info("Host: " + instanceConfig.getHostName(false) + ":" + instanceConfig.getNonSecurePort());
        logger.info("Application name: " + instanceConfig.getAppname());
        logger.info("===================================");

        manager.initComponent(instanceConfig, clientConfig);
        manager.getEurekaClient().registerHealthCheck(instanceStatus -> status);

        status = InstanceInfo.InstanceStatus.UP;

        return manager;
    }

    private EurekaClient initClient() {
        return getManager().getEurekaClient();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private class ServerInfo {
        private InstanceInfo info;
        private Long resolvedTimeMs;
    }

    private static class EurekaDisabled extends RuntimeException {
    }
}
