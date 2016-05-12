package ru.citeck.ecos.behavior.common.documentlibrary;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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
        final NodeRef nodeRef = childAssocRef.getChildRef();
        if (!nodeService.exists(nodeRef)) {
            return;
        }
        Boolean changeType = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Boolean>() {
            @Override
            public Boolean doWork() throws Exception {
                SiteInfo siteInfo = siteService.getSite(nodeRef);
                return isNodeInDocumentLibraryOnFileSite(siteInfo, nodeRef);
            }
        });

        if (changeType) {
            nodeService.setType(nodeRef, EcosModel.TYPE_DOCUMENT);
        }
    }

    private boolean isNodeInDocumentLibraryOnFileSite(SiteInfo siteInfo, NodeRef nodeRef) {
        if (siteInfo == null) {
            return false;
        }
        if (SiteModel.FILE_SITE_PRESET.equals(siteInfo.getSitePreset())) {
            return false;
        }
        return checkParentDocumentLibraryOnSite(siteInfo, nodeRef);
    }

    private boolean checkParentDocumentLibraryOnSite(SiteInfo siteInfo, NodeRef nodeRef) {
        NodeRef folderRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
        if (folderRef == null || siteInfo.getNodeRef().equals(folderRef)) {
            return false;
        }
        if (DOCUMENT_LIBRARY.equals(nodeService.getProperty(folderRef, ContentModel.PROP_NAME))) {
            return true;
        } else {
            return checkParentDocumentLibraryOnSite(siteInfo, folderRef);
        }
    }
}
