package ru.citeck.ecos.flowable.services;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

import java.util.Set;

/**
 * @author Roman Makarskiy
 */
public class FlowableRecipientsServiceJS extends AlfrescoScopableProcessorExtension {

    private FlowableRecipientsService flowableRecipientsService;

    public String getRoleEmails(Object document, String caseRoleName) {
        NodeRef docRef = JavaScriptImplUtils.getNodeRef(document);
        return flowableRecipientsService.getRoleEmails(docRef, caseRoleName);
    }

    public Set<String> geRoleGroups(Object document, String caseRoleName) {
        NodeRef docRef = JavaScriptImplUtils.getNodeRef(document);
        return flowableRecipientsService.getRoleGroups(docRef, caseRoleName);
    }

    public Set<String> geRoleUsers(Object document, String caseRoleName) {
        NodeRef docRef = JavaScriptImplUtils.getNodeRef(document);
        return flowableRecipientsService.getRoleUsers(docRef, caseRoleName);
    }

    public void setFlowableRecipientsService(FlowableRecipientsService flowableRecipientsService) {
        this.flowableRecipientsService = flowableRecipientsService;
    }
}
