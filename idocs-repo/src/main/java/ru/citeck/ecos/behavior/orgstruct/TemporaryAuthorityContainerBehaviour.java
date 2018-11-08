package ru.citeck.ecos.behavior.orgstruct;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.behavior.JavaBehaviour;
import ru.citeck.ecos.model.OrgStructModel;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemporaryAuthorityContainerBehaviour implements NodeServicePolicies.OnCreateNodePolicy {

    private static final Log logger = LogFactory.getLog(TemporaryAuthorityContainerBehaviour.class);

    private NodeService nodeService;
    private AuthorityService authorityService;
    private PolicyComponent policyComponent;

    private List<QName> aspectsToCopy;
    private List<QName> propsToCopy;
    private List<QName> assocsToCopy;

    public void init() {
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME,
                ContentModel.TYPE_AUTHORITY_CONTAINER, new JavaBehaviour(this, "onCreateNode",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        NodeRef authorityContainer = childAssocRef.getChildRef();
        if (nodeService.hasAspect(authorityContainer, OrgStructModel.ASPECT_IS_TEMPORARY_AUTHORITY_CONTAINER)) {
            NodeRef authorityContainerParent = childAssocRef.getParentRef();
            String authorityContainerParentName = RepoUtils.getProperty(authorityContainerParent,
                    ContentModel.PROP_AUTHORITY_NAME, nodeService);
            String authorityContainerName = RepoUtils.getProperty(authorityContainer,
                    ContentModel.PROP_AUTHORITY_NAME, nodeService);

            List<QName> aspects = new ArrayList<>();
            Map<QName, Serializable> props = new HashMap<>();
            Map<QName, List<NodeRef>> assocs = new HashMap<>();

            for (QName aspectToCopy : aspectsToCopy) {
                if (nodeService.hasAspect(authorityContainer, aspectToCopy)) {
                    aspects.add(aspectToCopy);
                }
            }

            for (QName propToCopy : propsToCopy) {
                Serializable value = nodeService.getProperty(authorityContainer, propToCopy);
                if (value != null) {
                    props.put(propToCopy, value);
                }
            }

            for (QName assocToCopy : assocsToCopy) {
                List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(authorityContainer, assocToCopy);
                if (targetAssocs.size() > 0) {
                    List<NodeRef> targetRefs = new ArrayList<>();
                    for (AssociationRef targetAssoc : targetAssocs) {
                        targetRefs.add(targetAssoc.getTargetRef());
                    }
                    assocs.put(assocToCopy, targetRefs);
                }
            }

            nodeService.deleteNode(authorityContainer);

            String fullName = authorityService.createAuthority(AuthorityType.GROUP, authorityContainerName);
            authorityService.addAuthority(authorityContainerParentName, fullName);

            NodeRef authorityRef = authorityService.getAuthorityNodeRef(fullName);

            for (QName aspect : aspects) {
                nodeService.addAspect(authorityRef, aspect, null);
            }

            for (Map.Entry<QName, Serializable> entry : props.entrySet()) {
                QName propQName = entry.getKey();
                Serializable value = entry.getValue();
                nodeService.setProperty(authorityRef, propQName, value);
            }

            for (Map.Entry<QName, List<NodeRef>> entry : assocs.entrySet()) {
                QName assocQName = entry.getKey();
                List<NodeRef> targetRefs = entry.getValue();
                nodeService.setAssociations(authorityRef, assocQName, targetRefs);
            }
        }
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setAspectsToCopy(List<QName> aspectsToCopy) {
        this.aspectsToCopy = aspectsToCopy;
    }

    public void setPropsToCopy(List<QName> propsToCopy) {
        this.propsToCopy = propsToCopy;
    }

    public void setAssocsToCopy(List<QName> assocsToCopy) {
        this.assocsToCopy = assocsToCopy;
    }
}
