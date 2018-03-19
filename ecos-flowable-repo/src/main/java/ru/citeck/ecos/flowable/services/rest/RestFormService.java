package ru.citeck.ecos.flowable.services.rest;

import org.codehaus.jackson.map.DeserializationConfig;
import org.flowable.form.model.FormModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.citeck.ecos.http.BasicAuthInterceptor;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RestFormService {

    @Value("${flowable.rest-api.username}")
    private String username;
    @Value("${flowable.rest-api.password}")
    private String password;
    @Value("${flowable.rest-api.url}")
    private String restApiUrl;

    @Value("${flowable.rest-api.form.form-definitions-by-key}")
    private String formDefinitionsByKeyUrl;
    @Value("${flowable.rest-api.form.form-model}")
    private String formModelUrl;

    private String baseUrl;
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        MappingJacksonHttpMessageConverter converter = new MappingJacksonHttpMessageConverter();
        converter.getObjectMapper().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(converter);

        restTemplate = new RestTemplate(converters);
        restTemplate.getInterceptors().add(new BasicAuthInterceptor(username, password));

        baseUrl = restApiUrl.endsWith("/") ? restApiUrl : restApiUrl + "/";
        baseUrl += "form-api/form-repository/";
    }

    public Optional<FormModel> getFormByKey(String formKey) {

        String url = baseUrl + formDefinitionsByKeyUrl;
        FormDefinitions definitions = restTemplate.getForObject(url, FormDefinitions.class, formKey);

        if (definitions.data != null && definitions.data.size() > 0) {

            String formDefinitionId = definitions.data.get(0).id;
            url = baseUrl + formModelUrl;

            return Optional.ofNullable(restTemplate.getForObject(url, FormModel.class, formDefinitionId));
        }

        return Optional.empty();
    }

    private static class FormDefinitions {

        public Integer total;
        public Integer start;
        public String sort;
        public String order;
        public Integer size;
        public List<Data> data;

        public static class Data {
            public String id;
            public String url;
            public String category;
            public String name;
            public String key;
            public String description;
            public Integer version;
            public String resourceName;
            public String deploymentId;
            public String parentDeploymentId;
            public String tenantId;
        }
    }
}
