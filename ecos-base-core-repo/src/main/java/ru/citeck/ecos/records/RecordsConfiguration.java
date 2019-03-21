package ru.citeck.ecos.records;

import org.alfresco.service.ServiceRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.citeck.ecos.graphql.AlfGqlContext;
import ru.citeck.ecos.predicate.PredicateService;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.RecordsServiceFactory;
import ru.citeck.ecos.records2.graphql.RecordsMetaGql;
import ru.citeck.ecos.records2.meta.RecordsMetaService;
import ru.citeck.ecos.records2.request.rest.RestQueryHandler;
import ru.citeck.ecos.records2.source.common.group.RecordsGroupDAO;

@Configuration
public class RecordsConfiguration extends RecordsServiceFactory {

    private ServiceRegistry serviceRegistry;

    @Bean
    public RecordsService createRecordsServiceBean(ServiceRegistry serviceRegistry,
                                                   RecordsMetaService recordsMetaService,
                                                   PredicateService predicateService) {

        this.serviceRegistry = serviceRegistry;

        RecordsServiceImpl recordsService = new RecordsServiceImpl(recordsMetaService, predicateService);
        recordsService.register(new RecordsGroupDAO());
        return recordsService;
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
    public RestQueryHandler createRestQueryHandler(RecordsService recordsService) {
        return new RestQueryHandler(recordsService);
    }

    @Override
    protected RecordsMetaGql createRecordsMetaGraphQL() {
        return new RecordsMetaGql(this.getGqlTypes(), () -> new AlfGqlContext(serviceRegistry));
    }
}
