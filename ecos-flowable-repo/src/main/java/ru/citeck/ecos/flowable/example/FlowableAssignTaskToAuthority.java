package ru.citeck.ecos.flowable.example;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authority.script.ScriptGroup;
import org.alfresco.repo.security.authority.script.ScriptUser;
import org.alfresco.repo.service.ServiceDescriptorRegistry;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.flowable.engine.common.api.delegate.Expression;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.engine.delegate.TaskListener;
import ru.citeck.ecos.providers.ApplicationContextProvider;

/**
 * Assign task to authority task listener
 */
public class FlowableAssignTaskToAuthority implements TaskListener {

    private Expression authority;

    @Override
    public void notify(DelegateTask delegateTask) {

        Object authorityObj = authority.getValue(delegateTask);

        if(authorityObj == null) return;

        ServiceRegistry services = ApplicationContextProvider.getBean(ServiceDescriptorRegistry.class);;
        if(services == null) return;

        NodeRef authorityRef = null;
        String authorityName = null;

        if(authorityObj instanceof NodeRef) {
            authorityRef = (NodeRef) authorityObj;
        } else if(authorityObj instanceof String) {
            String authorityString = (String) authorityObj;
            if(NodeRef.isNodeRef(authorityString)) {
                authorityRef = new NodeRef((String) authorityObj);
            } else {
                authorityName = authorityString;
            }
        } else if(authorityObj instanceof ScriptGroup) {
            authorityName = ((ScriptGroup)authorityObj).getFullName();
        } else if(authorityObj instanceof ScriptUser) {
            authorityName = ((ScriptUser)authorityObj).getUserName();
        } else if(authorityObj instanceof ScriptNode) {
            authorityRef = ((ScriptNode)authorityObj).getNodeRef();
        } else {
            throw new IllegalArgumentException("Can not convert value of type " + authorityObj.getClass().getName() + " to NodeRef");
        }

        if(authorityName == null) {

            if(authorityRef == null || !services.getNodeService().exists(authorityRef))
                return;

            QName authorityType = services.getNodeService().getType(authorityRef);
            if(services.getDictionaryService().isSubClass(authorityType, ContentModel.TYPE_PERSON))
            {
                authorityName = (String) services.getNodeService().getProperty(authorityRef, ContentModel.PROP_USERNAME);
            }
            else if(services.getDictionaryService().isSubClass(authorityType, ContentModel.TYPE_AUTHORITY_CONTAINER))
            {
                authorityName = (String) services.getNodeService().getProperty(authorityRef, ContentModel.PROP_AUTHORITY_NAME);
            }
            else
            {
                throw new IllegalArgumentException("Unknown authority type: " + authorityType + " of node " + authorityRef);
            }
        }

        if(authorityName.startsWith(AuthorityType.GROUP.getPrefixString())) {
            // if authority is group - add to candidate groups
            delegateTask.addCandidateGroup(authorityName);
        } else {
            // if authority is person - assign task to him/she
            delegateTask.setAssignee(authorityName);
        }

    }
}
