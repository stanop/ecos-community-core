package ru.citeck.ecos.eureka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
@Configuration
public class EurekaContextConfig {

    public static final String REST_TEMPLATE_ID = "eurekaRestTemplate";

    @Autowired
    @Qualifier("global-properties")
    private Properties properties;

    private boolean isDevEnv() {
        String isDevEnv = properties.getProperty("ecos.environment.dev", "false");
        if (Boolean.TRUE.toString().equals(isDevEnv)) {
            log.info("DEV ENV enabled for EurekaConfig");
            return true;
        }
        log.info("PROD ENV enabled for EurekaConfig");
        return false;
    }

    @Bean(name = REST_TEMPLATE_ID)
    public RestTemplate createRestTemplate(EcosEurekaClient client) {

        RestTemplate template = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = template.getInterceptors();
        if (interceptors == null) {
            interceptors = new ArrayList<>();
        } else {
            interceptors = new ArrayList<>(interceptors);
        }

        interceptors.add(new EurekaRequestInterceptor(client, isDevEnv()));
        template.setInterceptors(interceptors);

        return template;
    }
}
