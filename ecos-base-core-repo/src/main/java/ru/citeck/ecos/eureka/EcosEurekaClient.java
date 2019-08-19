package ru.citeck.ecos.eureka;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.discovery.EurekaClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class EcosEurekaClient {

    private static final Long INFO_CACHE_AGE = TimeUnit.SECONDS.toMillis(30L);
    private static final String ERROR_MSG = "Cannot get an instance of '%s' service from eureka";

    private Map<String, ServerInfo> serversInfo = new ConcurrentHashMap<>();

    @Autowired
    @Qualifier("global-properties")
    private Properties properties;

    @Getter(lazy = true) private final DiscoveryManager manager = initManager();
    @Getter(lazy = true) private final EurekaClient client = initClient();

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
        manager.initComponent(new MyDataCenterInstanceConfig(), new EurekaAlfClientConfig(properties));
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
}
