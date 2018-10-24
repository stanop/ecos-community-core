package ru.citeck.ecos.flowable.services;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

/**
 * @author Roman Makarskiy
 */
public interface FlowableRecipientsService {
    String getRoleEmails(NodeRef document, String caseRoleName);

    List<String> getRoleGroups(NodeRef document, String caseRoleName);

    List<String> getRoleUsers(NodeRef document, String caseRoleName);
}
