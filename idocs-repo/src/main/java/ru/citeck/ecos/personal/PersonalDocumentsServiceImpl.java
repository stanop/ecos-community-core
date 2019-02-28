package ru.citeck.ecos.personal;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class PersonalDocumentsServiceImpl implements PersonalDocumentsService {

    private static final Log logger = LogFactory.getLog(PersonalDocumentsServiceImpl.class);

    private NodeService nodeService;

    @Override
    public NodeRef ensureDirectory(String userName) {
        // TODO: Implement
        return null;
    }

    @Override
    public List<NodeRef> getDocuments(String userName) {
        // TODO: Implement
        return new ArrayList<>();
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
