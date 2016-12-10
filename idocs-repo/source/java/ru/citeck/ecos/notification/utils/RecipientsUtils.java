package ru.citeck.ecos.notification.utils;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.ICaseRoleModel;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Roman Makarskiy
 */
public class RecipientsUtils {

    private static Log logger = LogFactory.getLog(RecipientsUtils.class);

    public static Set<String> getRecipientsFromRole(List<String> roles, NodeRef iCase, NodeService nodeService) {
        Set<String> recipients = new HashSet<>();
        for (String recipientFromICaseRole : roles) {
            List<ChildAssociationRef> iCaseRoles = nodeService.getChildAssocs(iCase,
                    ICaseRoleModel.ASSOC_ROLES, RegexQNamePattern.MATCH_ALL);
            if (!iCaseRoles.isEmpty()) {
                NodeRef iCaseRole = getICaseRoleOrNullNotFound(recipientFromICaseRole, iCaseRoles, nodeService);
                if (iCaseRole != null && nodeService.exists(iCaseRole)) {
                    List<AssociationRef> recipientsRef = nodeService.getTargetAssocs(iCaseRole,
                            ICaseRoleModel.ASSOC_ASSIGNEES);
                    if (!recipientsRef.isEmpty()) {
                        for (AssociationRef recipientRef : recipientsRef) {
                            String recipient = getRecipientNameByPersonOrAuthority(recipientRef.getTargetRef(),
                                    nodeService);
                            if (recipient != null && !recipient.equals("")) {
                                recipients.add(recipient);
                            }
                        }
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Cannot find recipients in case : " + iCaseRole);
                        }
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Cannot find needed iCase role: " + recipientFromICaseRole
                                + " in document: " + iCase);
                    }
                }

            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("iCase Role is empty in document: " + iCase);
                }
            }
        }
        return recipients;
    }

    public static Set<String> getRecipientFromNodeAssoc(List<QName> assocs, NodeRef node, NodeService nodeService) {
        Set<String> assocRecipientsNames = new HashSet<>();
        for (QName recipient : assocs) {
            List<AssociationRef> recipientList = nodeService.getTargetAssocs(node, recipient);
            if (!recipientList.isEmpty()) {
                assocRecipientsNames.add(RecipientsUtils.getRecipientNameByPersonOrAuthority(recipientList.get(0).getTargetRef(),
                        nodeService));
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cannot find recipient: " + recipient + " for document: " + node);
                }
            }
        }
        return assocRecipientsNames;
    }

    public static String getRecipientNameByPersonOrAuthority(NodeRef nodeRef, NodeService nodeService) {
        String recipient = "";
        QName nodeType = nodeService.getType(nodeRef);

        if (nodeType.equals(ContentModel.TYPE_PERSON)) {
            recipient = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
        } else if (nodeType.equals(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
            recipient = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHORITY_NAME);
        }
        return recipient;
    }

    private static NodeRef getICaseRoleOrNullNotFound(String roleName, List<ChildAssociationRef> iCaseRoles,
                                                      NodeService nodeService) {
        for (ChildAssociationRef caseRole : iCaseRoles) {
            String foundRoleName = (String) nodeService.getProperty(caseRole.getChildRef(), ICaseRoleModel.PROP_VARNAME);
            if (Objects.equals(foundRoleName, roleName)) {
                return caseRole.getChildRef();
            }
        }
        return null;
    }

}
