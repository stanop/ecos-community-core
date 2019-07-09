package ru.citeck.ecos.records;

import org.alfresco.service.ServiceRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.citeck.ecos.graphql.AlfGqlContext;
import ru.citeck.ecos.predicate.PredicateService;
import ru.citeck.ecos.querylang.QueryLangService;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.RecordsServiceFactory;
import ru.citeck.ecos.records2.graphql.RecordsMetaGql;
import ru.citeck.ecos.records2.meta.RecordsMetaService;
import ru.citeck.ecos.records2.request.rest.RestHandler;
import ru.citeck.ecos.records2.resolver.RecordsResolver;
import ru.citeck.ecos.records2.source.common.group.RecordsGroupDAO;

@Configuration
public class RecordsConfiguration extends RecordsServiceFactory {

    private ServiceRegistry serviceRegistry;
    private RecordsServiceImpl recordsService;

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
    public RecordsMetaGql createRecordsMetaGraphQL() {
        return new RecordsMetaGql(this.getGqlTypes(), () -> new AlfGqlContext(serviceRegistry, recordsService));
    }
}
