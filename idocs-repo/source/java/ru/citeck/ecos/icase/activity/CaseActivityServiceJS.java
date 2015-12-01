package ru.citeck.ecos.icase.activity;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

import java.util.List;

/**
 * @author Pavel Simonov
 */
public class CaseActivityServiceJS extends AlfrescoScopableProcessorExtension {
    private static final Log log = LogFactory.getLog(CaseActivityServiceJS.class);

    private CaseActivityService caseActivityService;
    private NamespaceService namespaceService;

    public void startActivity(Object stageRef) {
        NodeRef ref = getNodeRef(stageRef);
        caseActivityService.startActivity(ref);
    }

    public void stopActivity(Object stageRef) {
        NodeRef ref = getNodeRef(stageRef);
        caseActivityService.stopActivity(ref);
    }

    public ScriptNode[] getActivities(Object nodeRef, String type) {
        NodeRef nRef = getNodeRef(nodeRef);
        QName typeQName = QName.createQName(type, namespaceService);
        List<NodeRef> stages = caseActivityService.getActivities(nRef, typeQName);
        return JavaScriptImplUtils.wrapNodes(stages, this);
    }

    public ScriptNode getDocument(Object nodeRef) {
        NodeRef ref = getNodeRef(nodeRef);
        NodeRef parent = caseActivityService.getDocument(ref);
        return JavaScriptImplUtils.wrapNode(parent, this);
    }

    private NodeRef getNodeRef(Object object) {
        if(object == null)
            return null;
        if(object instanceof NodeRef)
            return (NodeRef) object;
        if(object instanceof String)
            return new NodeRef((String) object);
        if(object instanceof ScriptNode)
            return ((ScriptNode) object).getNodeRef();
        throw new IllegalArgumentException("Can not convert from " + object.getClass() + " to NodeRef");
    }

    public void setCaseActivityService(CaseActivityService caseActivityService) {
        this.caseActivityService = caseActivityService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }
}
