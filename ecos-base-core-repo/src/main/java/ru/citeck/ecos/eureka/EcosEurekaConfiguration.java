package ru.citeck.ecos.eureka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.citeck.ecos.utils.InetUtils;

import java.util.Properties;

@Configuration
public class EcosEurekaConfiguration {

    @Autowired
    @Qualifier("global-properties")
    private Properties properties;

    @Autowired
    private InetUtils inetUtils;

    @Bean
    public EurekaAlfInstanceConfig getEurekaInstanceConfig() {
        return new EurekaAlfInstanceConfig(properties, inetUtils);
    }

    @Bean
    public EurekaAlfClientConfig getEurekaAlfClientConfig() {
        return new EurekaAlfClientConfig(properties);
    }
}
