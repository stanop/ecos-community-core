package ru.citeck.ecos.behavior.personal;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.behavior.base.AbstractBehaviour;
import ru.citeck.ecos.behavior.base.PolicyMethod;
import ru.citeck.ecos.model.ClassificationModel;
import ru.citeck.ecos.model.PersonalDocumentsModel;

public class SetDefaultTypeKindBehaviour extends AbstractBehaviour
                                         implements NodeServicePolicies.OnCreateChildAssociationPolicy  {

    private NodeRef defaultType;
    private NodeRef defaultKind;

    @Override
    protected void beforeInit() {
        setClassName(PersonalDocumentsModel.TYPE_PERSONAL_DOCUMENTS);
        setAssocName(ContentModel.ASSOC_CONTAINS);
    }

    @PolicyMethod(policy = NodeServicePolicies.OnCreateChildAssociationPolicy.class,
            frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT, runAsSystem = true)
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode) {
        NodeRef newPersonalDocument = childAssocRef.getChildRef();

        if (newPersonalDocument != null &&  nodeService.exists(newPersonalDocument)) {
            NodeRef type = (NodeRef) nodeService.getProperty(newPersonalDocument, ClassificationModel.PROP_DOCUMENT_TYPE);
            if (type == null && defaultType != null && nodeService.exists(defaultType)) {
                nodeService.setProperty(newPersonalDocument, ClassificationModel.PROP_DOCUMENT_TYPE, defaultType);
            }

            NodeRef kind = (NodeRef) nodeService.getProperty(newPersonalDocument, ClassificationModel.PROP_DOCUMENT_KIND);
            if (kind == null && defaultKind != null && nodeService.exists(defaultKind)) {
                nodeService.setProperty(newPersonalDocument, ClassificationModel.PROP_DOCUMENT_KIND, defaultKind);
            }
        }
    }

    public void setDefaultType(NodeRef defaultType) {
        this.defaultType = defaultType;
    }

    public void setDefaultKind(NodeRef defaultKind) {
        this.defaultKind = defaultKind;
    }

}
