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
import ru.citeck.ecos.predicate.model.AndPredicate;
import ru.citeck.ecos.predicate.model.Predicate;
import ru.citeck.ecos.predicate.model.ValuePredicate;
import ru.citeck.ecos.records.source.alf.search.CriteriaAlfNodesSearch;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.RecordsServiceFactory;
import ru.citeck.ecos.records2.graphql.RecordsMetaGql;
import ru.citeck.ecos.records2.meta.RecordsMetaService;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.SearchCriteriaParser;
import ru.citeck.ecos.search.SearchPredicate;

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

        RecordsService service = new RecordsServiceImpl(recordsMetaService, predicateService);

        service.register(this::convertCriteriaToPredicate,
                         CriteriaAlfNodesSearch.LANGUAGE,
                         RecordsService.LANGUAGE_PREDICATE);
        service.register(this::convertPredicateToCriteria,
                         RecordsService.LANGUAGE_PREDICATE,
                         CriteriaAlfNodesSearch.LANGUAGE);

        return service;
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

    private JsonNode convertCriteriaToPredicate(JsonNode node) {

        String criteriaStr;
        if (node.isTextual()) {
            criteriaStr = node.asText();
        } else {
            criteriaStr = node.toString();
        }

        return predicateService.writeJson(ValuePredicate.equal("criteria", criteriaStr));
    }

    private JsonNode convertPredicateToCriteria(JsonNode node) {

        Predicate predicate = predicateService.readJson(node);
        List<Predicate> predicates = ((AndPredicate) predicate).getPredicates();

        SearchCriteria searchCriteria = criteriaParser.parse(((ValuePredicate) predicates.get(0)).getValue());

        ValuePredicate groupPredicate = (ValuePredicate) predicates.get(1);

        QName groupQName = QName.resolveToQName(namespaceService, groupPredicate.getAttribute());
        String groupValue = ((String) ((ValuePredicate) predicates.get(1)).getValue());
        searchCriteria.addCriteriaTriplet(groupQName, SearchPredicate.STRING_EQUALS, groupValue);

        return objectMapper.valueToTree(searchCriteria);
    }
}
