package ru.citeck.ecos.flowable.services.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.flowable.services.FlowableRecipientsService;

import java.util.List;

/**
 * @author Roman Makarskiy
 */
public class FlowableRecipientsServiceImpl implements FlowableRecipientsService {
    @Override
    public String getRoleEmails(NodeRef document, String caseRoleName) {
        return null;
    }

    @Override
    public List<String> getRoleGroups(NodeRef document, String caseRoleName) {
        return null;
    }

    @Override
    public List<String> getRoleUsers(NodeRef document, String caseRoleName) {
        return null;
    }
}
