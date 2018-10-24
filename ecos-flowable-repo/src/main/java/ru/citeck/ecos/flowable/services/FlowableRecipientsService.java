package ru.citeck.ecos.flowable.services;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Set;

/**
 * @author Roman Makarskiy
 */
public interface FlowableRecipientsService {
    String getRoleEmails(NodeRef document, String caseRoleName);

    Set<String> getRoleGroups(NodeRef document, String caseRoleName);

    Set<String> getRoleUsers(NodeRef document, String caseRoleName);
}
