package ru.citeck.ecos.records;

import org.alfresco.service.ServiceRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.citeck.ecos.graphql.AlfGqlContext;
import ru.citeck.ecos.predicate.PredicateService;
import ru.citeck.ecos.records2.RecordsServiceFactory;
import ru.citeck.ecos.records2.graphql.RecordsMetaGql;
import ru.citeck.ecos.records2.meta.RecordsMetaService;

@Configuration
public class RecordsConfiguration extends RecordsServiceFactory {

    private ServiceRegistry serviceRegistry;

    @Bean
    public ru.citeck.ecos.records.RecordsServiceImpl createRecordsServiceBean(ServiceRegistry serviceRegistry,
                                                                              RecordsMetaService recordsMetaService,
                                                                              PredicateService predicateService) {
        this.serviceRegistry = serviceRegistry;
        return new RecordsServiceImpl(recordsMetaService, predicateService);
    }

    @Bean
    public PredicateService createPredicateService() {
        return super.createPredicateService();
    }

    @Bean
    public RecordsMetaService createRecordsMetaService() {
        return super.createRecordsMetaService();
    }

    @Override
    protected RecordsMetaGql createRecordsMetaGraphQL() {
        return new RecordsMetaGql(this.getGqlTypes(), () -> new AlfGqlContext(serviceRegistry));
    }
}
