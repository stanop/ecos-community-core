package ru.citeck.ecos.flowable.temp;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.flowable.engine.delegate.DelegateExecution;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;
import ru.citeck.ecos.workflow.confirm.ConfirmDecision;

import java.util.List;

/**
 * Confirm decisions js service
 */
public class FlowableConfirmDecisionsJS extends AlfrescoScopableProcessorExtension {

    private FlowableConfirmHelper impl;

    public void save(Object packageObj,
                     Object confirmerRole, String confirmTaskId)
    {
        impl.saveConfirmDecision(getWorkflowPackage(packageObj),
                getAuthorityName(confirmerRole), confirmTaskId);
    }

    public ConfirmDecision get(DelegateExecution execution, String confirmerRole)
    {
        return impl.getConfirmDecision(execution, getAuthorityName(confirmerRole));
    }

    public ConfirmDecision[] list(DelegateExecution execution) {
        List<ConfirmDecision> confirmDecisions = impl.getConfirmDecisions(execution);
        return confirmDecisions.toArray(new ConfirmDecision[2]);
    }

    public ConfirmDecision[] list(ScriptNode node) {
        List<ConfirmDecision> confirmDecisions = impl.getLatestConfirmDecisions(node.getNodeRef());
        return confirmDecisions.toArray(new ConfirmDecision[2]);
    }

    public ScriptNode[] listConfirmers(ScriptNode node) {
        return JavaScriptImplUtils.wrapAuthoritiesAsNodes(impl.getLatestConfirmers(node.getNodeRef()), this);
    }

    public ScriptNode[] listConfirmers(ScriptNode node, String outcome) {
        return JavaScriptImplUtils.wrapAuthoritiesAsNodes(impl.getLatestConfirmers(node.getNodeRef(), outcome), this);
    }

    private NodeRef getWorkflowPackage(Object obj) {
        if(obj == null) {
            return null;
        }
        if(obj instanceof NodeRef) {
            return (NodeRef) obj;
        }
        if(obj instanceof ScriptNode) {
            return ((ScriptNode)obj).getNodeRef();
        }
        if(obj instanceof DelegateExecution) {
            return FlowableListenerUtils.getWorkflowPackage((DelegateExecution)obj);
        }
        throw new IllegalArgumentException("Can not get workflow package for input object of class " + obj.getClass());
    }

    private String getAuthorityName(String string) {
        if(!NodeRef.isNodeRef(string)) {
            AuthorityService authorityService = serviceRegistry.getAuthorityService();
            if(!authorityService.authorityExists(string)) {
                throw new IllegalArgumentException("Authority does not exist: " + string);
            }
            return string;
        }
        NodeRef nodeRef = new NodeRef(string);
        return getAuthorityName(nodeRef);
    }

    private String getAuthorityName(NodeRef nodeRef) {
        NodeService nodeService = serviceRegistry.getNodeService();
        if(!nodeService.exists(nodeRef)) {
            throw new IllegalArgumentException("Node does not exist: " + nodeRef);
        }
        QName type = nodeService.getType(nodeRef);
        if(type.equals(ContentModel.TYPE_PERSON)) {
            return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
        }
        if(type.equals(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
            return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHORITY_NAME);
        }
        throw new IllegalArgumentException("Node type is neither cm:person, nor cm:authorityContainer, but " +
                type + " for node " + nodeRef);
    }

    private String getAuthorityName(Object obj) {
        if(obj == null) {
            return null;
        }
        if(obj instanceof NodeRef) {
            return getAuthorityName((NodeRef) obj);
        }
        if(obj instanceof ScriptNode) {
            return getAuthorityName(((ScriptNode)obj).getNodeRef());
        }
        if(obj instanceof String) {
            return getAuthorityName((String) obj);
        }
        throw new IllegalArgumentException("Can not get authority name for input object of class " + obj.getClass());
    }

    public void setImpl(FlowableConfirmHelper impl) {
        this.impl = impl;
    }
}
