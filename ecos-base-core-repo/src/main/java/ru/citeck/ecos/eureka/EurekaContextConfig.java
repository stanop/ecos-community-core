package ru.citeck.ecos.eureka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class EurekaContextConfig {


    public static final String REST_TEMPLATE_ID = "eurekaRestTemplate";

    @Bean(name = REST_TEMPLATE_ID)
    public RestTemplate createRestTemplate(EcosEurekaClient client) {

        RestTemplate template = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = template.getInterceptors();
        if (interceptors == null) {
            interceptors = new ArrayList<>();
        } else {
            interceptors = new ArrayList<>(interceptors);
        }

        interceptors.add(new EurekaRequestInterceptor(client));
        template.setInterceptors(interceptors);

        return template;
    }
}
