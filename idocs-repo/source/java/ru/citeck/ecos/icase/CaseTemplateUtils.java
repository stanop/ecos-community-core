package ru.citeck.ecos.icase;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.search.*;

import java.util.List;

/**
 * @author Maxim Strizhov
 */
public final class CaseTemplateUtils {

    public static List<NodeRef> getTemplatesForType(QName type,
                                                    SearchCriteriaFactory factory,
                                                    CriteriaSearchService searchService) {
        SearchCriteria searchCriteria = factory.createSearchCriteria()
                .addCriteriaTriplet(FieldType.TYPE, SearchPredicate.TYPE_EQUALS, ICaseModel.TYPE_CASE_TEMPLATE)
                .addCriteriaTriplet(ICaseModel.PROP_CASE_TYPE, SearchPredicate.STRING_EQUALS, type.toString());
        return searchService.query(searchCriteria, SearchService.LANGUAGE_LUCENE).getResults();
    }
}
