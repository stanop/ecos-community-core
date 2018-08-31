package ru.citeck.ecos.graphql.journal.datasource.alfnode;

import com.google.common.collect.Lists;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.journal.datasource.RecordsDataSource;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeInfo;
import ru.citeck.ecos.graphql.meta.alfnode.AlfNodeRecord;
import ru.citeck.ecos.records.RecordsService;
import ru.citeck.ecos.records.source.alfnode.AlfNodesRecordsDAO;

import java.util.*;

public class AlfNodesDataSource extends RecordsDataSource {

    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;

    @Autowired
    public AlfNodesDataSource(ServiceRegistry serviceRegistry,
                              RecordsService recordsService) {

        super(AlfNodesRecordsDAO.ID);

        this.namespaceService = serviceRegistry.getNamespaceService();
        this.dictionaryService = serviceRegistry.getDictionaryService();
    }

    @Override
    public Optional<JGqlAttributeInfo> getAttributeInfo(String attributeName) {
        return Optional.of(new AlfNodeAttributeInfo(attributeName, namespaceService, dictionaryService));
    }

    @Override
    public List<String> getDefaultAttributes() {
        return Lists.newArrayList(AlfNodeRecord.ATTR_ASPECTS,
                                  AlfNodeRecord.ATTR_IS_DOCUMENT,
                                  AlfNodeRecord.ATTR_IS_CONTAINER);
    }
}
