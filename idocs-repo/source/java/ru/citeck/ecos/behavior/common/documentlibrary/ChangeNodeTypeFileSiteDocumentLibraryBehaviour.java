package ru.citeck.ecos.behavior.common.documentlibrary;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import ru.citeck.ecos.model.EcosModel;
import ru.citeck.ecos.model.SiteModel;

public class ChangeNodeTypeFileSiteDocumentLibraryBehaviour implements NodeServicePolicies.OnCreateNodePolicy {

    public static final String DOCUMENT_LIBRARY = "documentLibrary";

    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private SiteService siteService;

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void init() {
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        NodeRef nodeRef = childAssocRef.getChildRef();
        if (!nodeService.exists(nodeRef)) {
            return;
        }
        SiteInfo siteInfo = siteService.getSite(nodeRef);
        NodeRef folderRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
        NodeRef parentFolder = nodeService.getPrimaryParent(folderRef).getParentRef();
        if (siteInfo == null || !nodeService.exists(folderRef)) {
            return;
        }
        if (SiteModel.FILE_SITE_PRESET.equals(siteInfo.getSitePreset())
                && (
                    DOCUMENT_LIBRARY.equals(nodeService.getProperty(folderRef, ContentModel.PROP_NAME))
                    || DOCUMENT_LIBRARY.equals(nodeService.getProperty(parentFolder, ContentModel.PROP_NAME))
                )
        ) {
            nodeService.setType(nodeRef, EcosModel.TYPE_DOCUMENT);
        }
    }
}
