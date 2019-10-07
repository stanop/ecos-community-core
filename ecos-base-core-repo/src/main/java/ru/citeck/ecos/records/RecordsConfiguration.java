package ru.citeck.ecos.records;

import org.alfresco.service.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.citeck.ecos.eureka.EurekaContextConfig;
import ru.citeck.ecos.graphql.AlfGqlContext;
import ru.citeck.ecos.predicate.PredicateService;
import ru.citeck.ecos.querylang.QueryLangService;
import ru.citeck.ecos.records2.QueryContext;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.RecordsServiceFactory;
import ru.citeck.ecos.records2.graphql.RecordsMetaGql;
import ru.citeck.ecos.records2.meta.RecordsMetaService;
import ru.citeck.ecos.records2.request.rest.RestHandler;
import ru.citeck.ecos.records2.resolver.RecordsResolver;
import ru.citeck.ecos.records2.resolver.RemoteRecordsResolver;
import ru.citeck.ecos.records2.source.dao.remote.RecordsRestConnection;

import java.util.function.Supplier;

@Configuration
public class RecordsConfiguration extends RecordsServiceFactory {

    @Autowired
    private ServiceRegistry serviceRegistry;

    @Autowired
    @Qualifier(EurekaContextConfig.REST_TEMPLATE_ID)
    private RestTemplate eurekaRestTemplate;

    @Bean
    public RecordsService createRecordsServiceBean() {
        return new RecordsServiceImpl(this);
    }

    @Bean
    public RecordsResolver createRecordsResolver() {
        return super.createRecordsResolver();
    }

    @Override
    protected RemoteRecordsResolver createRemoteRecordsResolver() {

        return new RemoteRecordsResolver(new RecordsRestConnection() {
            @Override
            public <T> T jsonPost(String url, Object body, Class<T> respType) {
                return eurekaRestTemplate.postForObject("http:/" + url, body, respType);
            }
        });
    }

    @Bean
    public QueryLangService createQueryLangService() {
        return super.createQueryLangService();
    }

    @Bean
    public PredicateService createPredicateService() {
        return super.createPredicateService();
    }

    @Bean
    public RecordsMetaService createRecordsMetaService() {
        return super.createRecordsMetaService();
    }

    @Bean
    public RestHandler createRestHandler(RecordsService recordsService) {
        return new RestHandler(recordsService);
    }

    @Override
    protected Supplier<? extends QueryContext> createQueryContextSupplier() {
        return () -> new AlfGqlContext(serviceRegistry);
    }

    @Override
    public RecordsMetaGql createRecordsMetaGql() {
        return super.createRecordsMetaGql();
    }
}
