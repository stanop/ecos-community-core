package ru.citeck.ecos.flowable.services.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.flowable.services.FlowableRecipientsService;
import ru.citeck.ecos.role.CaseRoleService;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Roman Makarskiy
 */
public class FlowableRecipientsServiceImpl implements FlowableRecipientsService {

    private static final Logger logger = Logger.getLogger(FlowableRecipientsServiceImpl.class);

    @Autowired
    private CaseRoleService caseRoleService;
    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    protected NodeService nodeService;

    @Override
    public String getRoleEmails(NodeRef document, String caseRoleName) {
        if (document == null || !nodeService.exists(document)) {
            throw new IllegalArgumentException("Document does not exist: " + document);
        }

        if (StringUtils.isBlank(caseRoleName)) {
            throw new IllegalArgumentException("CaseRoleName must be specified");
        }

        Set<NodeRef> assignees = caseRoleService.getAssignees(document, caseRoleName);
        Set<String> emails = new HashSet<>();

        for (NodeRef assignee : assignees) {
            if (nodeService.exists(assignee)) {
                QName type = nodeService.getType(assignee);
                if (dictionaryService.isSubClass(type, ContentModel.TYPE_PERSON)) {
                    String email = RepoUtils.getProperty(assignee, ContentModel.PROP_EMAIL, nodeService);
                    if (StringUtils.isNotBlank(email)) {
                        emails.add(email);
                    }
                } else if (dictionaryService.isSubClass(type, ContentModel.TYPE_AUTHORITY_CONTAINER)) {
                    String groupName = (String) nodeService.getProperty(assignee, ContentModel.PROP_AUTHORITY_NAME);
                    Set<String> authorities = authorityService.getContainedAuthorities(AuthorityType.USER, groupName, false);
                    for (String authority : authorities) {
                        NodeRef authorityRef = authorityService.getAuthorityNodeRef(authority);
                        String email = RepoUtils.getProperty(authorityRef, ContentModel.PROP_EMAIL, nodeService);
                        if (StringUtils.isNotBlank(email)) {
                            emails.add(email);
                        }
                    }
                }
            }
        }

        if (!emails.isEmpty()) {
            return String.join(EMAIL_SEPARATOR, emails);
        } else {
            return "";
        }
    }

    @Override
    public Set<String> getRoleGroups(NodeRef document, String caseRoleName) {
        return getRoleRecipients(document, caseRoleName, ContentModel.TYPE_AUTHORITY_CONTAINER,
                ContentModel.PROP_AUTHORITY_NAME);
    }

    @Override
    public Set<String> getRoleUsers(NodeRef document, String caseRoleName) {
        return getRoleRecipients(document, caseRoleName, ContentModel.TYPE_PERSON,
                ContentModel.PROP_USERNAME);
    }

    private Set<String> getRoleRecipients(NodeRef document, String caseRoleName, QName recipientType,
                                          QName recipientNameProp) {
        if (document == null || !nodeService.exists(document)) {
            throw new IllegalArgumentException("Document does not exist: " + document);
        }

        if (StringUtils.isBlank(caseRoleName)) {
            throw new IllegalArgumentException("CaseRoleName must be specified");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Getting role recipients, document: " + document + ", caseRoleName: " + caseRoleName);
        }

        Set<String> recipients = new HashSet<>();
        Set<NodeRef> assignees = caseRoleService.getAssignees(document, caseRoleName);

        for (NodeRef assignee : assignees) {
            if (nodeService.exists(assignee)) {
                QName type = nodeService.getType(assignee);
                if (dictionaryService.isSubClass(type, recipientType)) {
                    String name = RepoUtils.getProperty(assignee, recipientNameProp, nodeService);
                    recipients.add(name);
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Return recipients: " + recipients);
        }

        return recipients;
    }
}
