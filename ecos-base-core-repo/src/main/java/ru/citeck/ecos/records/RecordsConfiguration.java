package ru.citeck.ecos.records;

import com.netflix.discovery.converters.Auto;
import org.alfresco.service.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.citeck.ecos.eureka.EurekaAlfClientConfig;
import ru.citeck.ecos.eureka.EurekaContextConfig;
import ru.citeck.ecos.graphql.AlfGqlContext;
import ru.citeck.ecos.predicate.PredicateService;
import ru.citeck.ecos.querylang.QueryLangService;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.RecordsServiceFactory;
import ru.citeck.ecos.records2.graphql.RecordsMetaGql;
import ru.citeck.ecos.records2.meta.RecordsMetaService;
import ru.citeck.ecos.records2.request.rest.RestHandler;
import ru.citeck.ecos.records2.resolver.RecordsResolver;
import ru.citeck.ecos.records2.resolver.RemoteRecordsResolver;
import ru.citeck.ecos.records2.source.common.group.RecordsGroupDAO;
import ru.citeck.ecos.records2.source.dao.remote.RecordsRestConnection;

@Configuration
public class RecordsConfiguration extends RecordsServiceFactory {

    private ServiceRegistry serviceRegistry;
    private RecordsServiceImpl recordsService;
    private QueryLangService queryLangService;
    private PredicateService predicateService;
    private RecordsMetaService recordsMetaService;

    @Autowired
    @Qualifier(EurekaContextConfig.REST_TEMPLATE_ID)
    private RestTemplate eurekaRestTemplate;

    @Bean
    public RecordsService createRecordsServiceBean(ServiceRegistry serviceRegistry,
                                                   RecordsMetaService recordsMetaService,
                                                   RecordsResolver recordsResolver) {

        this.serviceRegistry = serviceRegistry;

        recordsService = new RecordsServiceImpl(recordsMetaService, recordsResolver);
        recordsService.register(new RecordsGroupDAO());
        return recordsService;
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
        if (queryLangService == null) {
            queryLangService = super.createQueryLangService();
        }
        return queryLangService;
    }

    @Bean
    public PredicateService createPredicateService() {
        if (predicateService == null) {
            predicateService = super.createPredicateService();
        }
        return predicateService;
    }

    @Bean
    public RecordsMetaService createRecordsMetaService() {
        if (recordsMetaService == null) {
            recordsMetaService = super.createRecordsMetaService();
        }
        return recordsMetaService;
    }

    @Bean
    public RestHandler createRestHandler(RecordsService recordsService) {
        return new RestHandler(recordsService);
    }

    @Override
    public RecordsMetaGql createRecordsMetaGraphQL() {
        return new RecordsMetaGql(this.getGqlTypes(), () -> new AlfGqlContext(serviceRegistry, recordsService));
    }
}
