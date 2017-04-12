package ru.citeck.ecos.notification.utils;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.ICaseRoleModel;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.*;

/**
 * @author Roman Makarskiy
 */
public class RecipientsUtils {

    private static Log logger = LogFactory.getLog(RecipientsUtils.class);

    public static Set<String> getRecipientsFromRole(List<String> roles, NodeRef iCase, NodeService nodeService,
                                                                        DictionaryService dictionaryService) {
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
                            addRecipient(recipients, recipientRef.getTargetRef(), nodeService, dictionaryService);
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

    public static Set<String> getRecipientFromNodeAssoc(List<QName> assocs, NodeRef node, NodeService nodeService,
                                                                            DictionaryService dictionaryService) {
        Set<String> assocRecipientsNames = new HashSet<>();
        for (QName recipient : assocs) {
            List<AssociationRef> recipientList = nodeService.getTargetAssocs(node, recipient);
            if (!recipientList.isEmpty()) {
                addRecipient(assocRecipientsNames, recipientList.get(0).getTargetRef(), nodeService, dictionaryService);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cannot find recipient: " + recipient + " for document: " + node);
                }
            }
        }
        return assocRecipientsNames;
    }

    public static Set<String> getRecipientsToExclude(List<String> ecludeRecipients, NodeRef node, ServiceRegistry serviceRegistry) {
        Set<String> recipientsToExclude = new HashSet<>();
        for (String recipient : ecludeRecipients) {
            if ("yourself".equals(recipient)) {
                String currentUser = serviceRegistry.getAuthenticationService().getCurrentUserName();
                addRecipient(recipientsToExclude, serviceRegistry.getPersonService().getPerson(currentUser),
                        serviceRegistry.getNodeService(), serviceRegistry.getDictionaryService());
            } else if (recipient.contains(":")) {
                List<QName> qNameList = Arrays.asList(QName.resolveToQName(serviceRegistry.getNamespaceService(), recipient));
                recipientsToExclude.addAll(
                        getRecipientFromNodeAssoc(
                                qNameList,
                                node,
                                serviceRegistry.getNodeService(),
                                serviceRegistry.getDictionaryService()
                        )
                );
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cannot find recipient: " + recipient + " for document: " + node);
                }
            }
        }
        return recipientsToExclude;
    }


    private static void addRecipient(Set<String> recipients, NodeRef recipientRef, NodeService nodeService,
                                                                                   DictionaryService dictionaryService) {

        String authorityName = RepoUtils.getAuthorityName(recipientRef, nodeService, dictionaryService);
        if (StringUtils.isNotBlank(authorityName)) {
            recipients.add(authorityName);
        }
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
