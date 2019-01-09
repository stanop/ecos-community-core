package ru.citeck.ecos.records.source.alfnode;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Map;

public class AlfNodesDefaultParentRegistrar {

    private AlfNodesRecordsDAO alfNodesRecordsDAO;
    private Map<QName, NodeRef> defaultParents;

    @PostConstruct
    void register() {
        if (defaultParents != null) {
            alfNodesRecordsDAO.registerDefaultParentByType(defaultParents);
        }
    }

    public void setDefaultParents(Map<QName, NodeRef> defaultParents) {
        this.defaultParents = defaultParents;
    }

    @Autowired
    public void setAlfNodesRecordsDAO(AlfNodesRecordsDAO alfNodesRecordsDAO) {
        this.alfNodesRecordsDAO = alfNodesRecordsDAO;
    }
}
