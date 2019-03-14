package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.citeck.ecos.graphql.AlfGqlContext;
import ru.citeck.ecos.predicate.PredicateService;
import ru.citeck.ecos.predicate.model.*;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.RecordsServiceFactory;
import ru.citeck.ecos.records2.graphql.RecordsMetaGql;
import ru.citeck.ecos.records2.meta.RecordsMetaService;
import ru.citeck.ecos.search.*;

import java.util.List;

@Configuration
public class RecordsConfiguration extends RecordsServiceFactory {

    private ServiceRegistry serviceRegistry;
    private PredicateService predicateService;
    private SearchCriteriaParser criteriaParser;
    private NamespaceService namespaceService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public RecordsService createRecordsServiceBean(ServiceRegistry serviceRegistry,
                                                   RecordsMetaService recordsMetaService,
                                                   PredicateService predicateService,
                                                   SearchCriteriaParser criteriaParser,
                                                   NamespaceService namespaceService) {

        this.serviceRegistry = serviceRegistry;
        this.criteriaParser = criteriaParser;
        this.namespaceService = namespaceService;

        return new RecordsServiceImpl(recordsMetaService, predicateService);
    }

    @Bean
    public PredicateService createPredicateService() {
        predicateService = super.createPredicateService();
        return predicateService;
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
