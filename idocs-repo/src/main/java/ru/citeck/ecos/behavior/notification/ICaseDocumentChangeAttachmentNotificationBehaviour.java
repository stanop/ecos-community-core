package ru.citeck.ecos.behavior.notification;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.*;
import org.alfresco.repo.version.VersionServicePolicies;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.ClassificationModel;
import ru.citeck.ecos.model.ICaseModel;

import java.util.HashMap;

/**
 * @author Roman.Makarskiy on 10/21/2016.
 */
public class ICaseDocumentChangeAttachmentNotificationBehaviour extends AbstractICaseDocumentNotificationBehaviour
        implements NodeServicePolicies.OnCreateChildAssociationPolicy,
        NodeServicePolicies.BeforeDeleteNodePolicy,
        VersionServicePolicies.AfterCreateVersionPolicy {

    private PolicyComponent policyComponent;
    private String documentNamespace;

    private String documentType;
    private QName documentQName;
    private HashMap<String, Object> addition;

    private final static String PARAM_TYPE = "type";
    private final static String PARAM_KIND = "kind";
    private final static String PARAM_FILE_NAME = "fileName";
    private final static String PARAM_METHOD = "method";
    private final static String PARAM_METHOD_ON_DELETE = "onDelete";
    private final static String PARAM_METHOD_ON_CREATE = "onCreate";
    private final static String PARAM_METHOD_UPLOAD_NEW_VERSION = "uploadNewVersion";

    public void init() {
        documentQName = QName.createQName(documentNamespace, documentType);

        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME,
                documentQName, ICaseModel.ASSOC_DOCUMENTS,
                new OrderedBehaviour(this, "onCreateChildAssociation",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT, order)
        );

        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
                ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "beforeDeleteNode", Behaviour.NotificationFrequency.EVERY_EVENT)
        );

        this.policyComponent.bindClassBehaviour(
                VersionServicePolicies.AfterCreateVersionPolicy.QNAME,
                ContentModel.TYPE_CONTENT,
                new OrderedBehaviour(this, "afterCreateVersion",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT, order)
        );
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssociationRef, boolean b) {
        NodeRef childRef = childAssociationRef.getChildRef();
        NodeRef caseRef = null;

        if (nodeService.getParentAssocs(childRef) != null) {
            caseRef = nodeService.getParentAssocs(childRef).get(0).getParentRef();
        }

        if (sender == null || !nodeService.exists(childRef) || !isActive(caseRef)) {
            return;
        }

        addition = new HashMap<>();
        addition = addTypeAndKind(childRef, addition);
        addition.put(PARAM_METHOD, PARAM_METHOD_ON_CREATE);
        sender.setAdditionArgs(addition);
        sender.sendNotification(childAssociationRef.getParentRef(), childRef, recipients,
                notificationType, subjectTemplate);
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef) {
        if (!nodeService.exists(nodeRef) || nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) || !isEnabled()) {
            return;
        }

        ChildAssociationRef primaryParent = nodeService.getPrimaryParent(nodeRef);
        NodeRef parentRef = primaryParent.getParentRef();

        if (nodeService.exists(parentRef) && primaryParent.getTypeQName().equals(ICaseModel.ASSOC_DOCUMENTS)) {
            QName parentQName = nodeService.getType(parentRef);
            if (sender != null && parentQName.equals(documentQName)) {
                String fileName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                addition = new HashMap<>();
                addition = addTypeAndKind(nodeRef, addition);
                addition.put(PARAM_METHOD, PARAM_METHOD_ON_DELETE);
                addition.put(PARAM_FILE_NAME, fileName);
                sender.setAdditionArgs(addition);
                sender.sendNotification(parentRef, nodeRef, recipients,
                        notificationType, subjectTemplate, true);
            }
        }
    }

    @Override
    public void afterCreateVersion(NodeRef nodeRef, Version version) {
        if (!nodeService.exists(nodeRef) || !isEnabled()) {
            return;
        }

        ChildAssociationRef primaryParent = nodeService.getPrimaryParent(nodeRef);
        NodeRef parentRef = primaryParent.getParentRef();

        if (nodeService.exists(parentRef) && primaryParent.getTypeQName().equals(ICaseModel.ASSOC_DOCUMENTS)
                && nodeService.getType(parentRef).equals(documentQName)) {
            addition = new HashMap<>();
            addition = addTypeAndKind(nodeRef, addition);
            addition.put(PARAM_METHOD, PARAM_METHOD_UPLOAD_NEW_VERSION);
            sender.setAdditionArgs(addition);
            sender.sendNotification(parentRef, nodeRef, recipients,
                    notificationType, subjectTemplate);
        }
    }

    private HashMap<String, Object> addTypeAndKind(NodeRef nodeRef, HashMap<String, Object> addition) {
        if (nodeService.exists(nodeRef)) {
            NodeRef documentType = (NodeRef) nodeService.getProperty(nodeRef,
                    ClassificationModel.PROP_DOCUMENT_TYPE);
            if (documentType != null) {
                addition.put(PARAM_TYPE, documentType);
            }
            NodeRef documentKind = (NodeRef) nodeService.getProperty(nodeRef,
                    ClassificationModel.PROP_DOCUMENT_KIND);
            if (documentKind != null) {
                addition.put(PARAM_KIND, documentKind);
            }
        }
        return addition;
    }

    public void setDocumentNamespace(String documentNamespace) {
        this.documentNamespace = documentNamespace;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

}
