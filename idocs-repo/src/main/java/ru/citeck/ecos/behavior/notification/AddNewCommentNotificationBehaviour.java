package ru.citeck.ecos.behavior.notification;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;

/**
 * @author Roman.Makarskiy on 10/27/2016.
 */
public class AddNewCommentNotificationBehaviour extends AbstractICaseDocumentNotificationBehaviour
        implements NodeServicePolicies.OnCreateChildAssociationPolicy {

    private PersonService personService;
    private ContentService contentService;
    private String documentNamespace;
    private String documentType;

    private final static String PARAM_COMMENT_CREATOR_NAME = "commentCreatorName";
    private final static String PARAM_COMMENT = "comment";

    private static Log logger = LogFactory.getLog(AddNewCommentNotificationBehaviour.class);

    public void init() {
        OrderedBehaviour createBehaviour = new OrderedBehaviour(
                this, "onCreateChildAssociation",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT, order
        );
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME,
                ForumModel.TYPE_TOPIC,
                ContentModel.ASSOC_CONTAINS,
                createBehaviour
        );
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssociationRef, boolean b) {
        NodeRef postRef = childAssociationRef.getChildRef();
        NodeRef documentRef = getDocumentByTopicRef(childAssociationRef.getParentRef());
        QName expectedDocumentType = QName.createQName(documentNamespace, documentType);

        if (!isActive(documentRef)) {
            return;
        }

        if (!nodeService.exists(documentRef)) {
            if (logger.isWarnEnabled()) {
                logger.warn("Cannot find document for topicRef: " + childAssociationRef.getParentRef());
            }
            return;
        }

        if (!nodeService.getType(documentRef).equals(expectedDocumentType)) {
            return;
        }

        HashMap<String, Object> additional = new HashMap<>();
        additional.put(PARAM_COMMENT_CREATOR_NAME, getCommentCreatorName(postRef));
        additional.put(PARAM_COMMENT, getCommentText(postRef));

        sender.setAdditionArgs(additional);
        sender.sendNotification(
                documentRef,
                postRef,
                recipients,
                notificationType,
                subjectTemplate
        );
    }

    private String getCommentText(NodeRef commentRef) {
        String commentContent = "";
        ContentReader reader = contentService.getReader(commentRef, ContentModel.PROP_CONTENT);
        if (reader != null) {
            commentContent = reader.getContentString();
        } else {
            logger.error("Failed to get text from comment: " + commentRef);
        }
        return commentContent;
    }

    private String getCommentCreatorName(NodeRef postRef) {
        String creatorName;
        String commentCreator = (String) nodeService.getProperty(postRef, ContentModel.PROP_CREATOR);
        NodeRef commentCreatorRef = personService.getPerson(commentCreator);
        String firstName = (String) nodeService.getProperty(commentCreatorRef, ContentModel.PROP_FIRSTNAME);
        String lastName = (String) nodeService.getProperty(commentCreatorRef, ContentModel.PROP_LASTNAME);
        creatorName = firstName + " " + lastName;
        return creatorName;
    }

    private NodeRef getDocumentByTopicRef(NodeRef topicRef) {
        NodeRef documentRef = null;
        NodeRef forumRef = null;

        List<ChildAssociationRef> forumRefs = nodeService.getParentAssocs(topicRef, ContentModel.ASSOC_CONTAINS,
                RegexQNamePattern.MATCH_ALL
        );
        if (!forumRefs.isEmpty()) {
            forumRef = forumRefs.get(0).getParentRef();
        }

        List<ChildAssociationRef> documentRefs = nodeService.getParentAssocs(forumRef, ForumModel.ASSOC_DISCUSSION,
                RegexQNamePattern.MATCH_ALL);

        if (!documentRefs.isEmpty()) {
            documentRef = documentRefs.get(0).getParentRef();
        }

        return documentRef;
    }

    public void setDocumentNamespace(String documentNamespace) {
        this.documentNamespace = documentNamespace;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

}
