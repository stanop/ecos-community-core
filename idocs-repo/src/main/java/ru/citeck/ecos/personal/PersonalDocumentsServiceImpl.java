package ru.citeck.ecos.personal;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import ru.citeck.ecos.model.OrgStructModel;
import ru.citeck.ecos.model.PersonalDocumentsModel;
import ru.citeck.ecos.utils.LazyNodeRef;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PersonalDocumentsServiceImpl extends AbstractLifecycleBean implements PersonalDocumentsService {

    private static final Log logger = LogFactory.getLog(PersonalDocumentsServiceImpl.class);

    private static final String GROUP_PERSONAL_DOCUMENTS_MANAGERS = "GROUP_personal_documents_managers";

    private AuthorityService authorityService;
    private NodeService nodeService;
    private PermissionService permissionService;

    private LazyNodeRef personalDocumentsRoot;
    private List<String> skipPersons = new ArrayList<>();

    @Override
    protected void onBootstrap(ApplicationEvent event) {
        AuthenticationUtil.runAsSystem(() -> {
            ensureAllPersonalDirectories();
            return null;
        });
    }

    @Override
    protected void onShutdown(ApplicationEvent event) {
        // nothing to do
    }

    @Override
    public NodeRef ensureDirectory(String userName) {
        ParameterCheck.mandatoryString("userName", userName);

        NodeRef userPersonRef = authorityService.getAuthorityNodeRef(userName);
        String userFullName = RepoUtils.getPersonFullName(userPersonRef, nodeService);
        NodeRef personalDocsDir = null;

        if (userPersonRef != null && nodeService.exists(userPersonRef)) {

            NodeRef personalDocsRef = RepoUtils.getFirstTargetAssoc(userPersonRef,
                    PersonalDocumentsModel.TYPE_PERSONAL_DOCUMENTS,
                    nodeService);

            if (personalDocsRef == null || !nodeService.exists(personalDocsRef)) {
                NodeRef personalDocsRootRef = personalDocumentsRoot.getNodeRef();

                NodeRef personalDocumentsFolderRef = nodeService.getChildByName(personalDocsRootRef,
                        ContentModel.ASSOC_CONTAINS,
                        userName);

                if (personalDocumentsFolderRef != null && nodeService.exists(personalDocumentsFolderRef)) {
                    boolean isAssociated = RepoUtils.isAssociated(userPersonRef,
                            personalDocumentsFolderRef,
                            OrgStructModel.ASSOC_PERSONAL_DOCUMENTS,
                            nodeService);

                    if (!isAssociated) {
                        createAssocToPersonalDocs(userPersonRef, personalDocumentsFolderRef);
                    }
                } else {
                    personalDocumentsFolderRef = RepoUtils.createChildWithName(personalDocsRootRef,
                            ContentModel.ASSOC_CONTAINS,
                            PersonalDocumentsModel.TYPE_PERSONAL_DOCUMENTS,
                            userName,
                            nodeService);

                    nodeService.setProperty(personalDocumentsFolderRef,
                            ContentModel.PROP_TITLE,
                            userFullName);

                    createAssocToPersonalDocs(userPersonRef, personalDocumentsFolderRef);
                    setPermissions(personalDocumentsFolderRef, userName);
                }
            }
        }
        return personalDocsDir;
    }

    @Override
    public List<NodeRef> getDocuments(String userName) {
        ParameterCheck.mandatoryString("userName", userName);

        List<NodeRef> docs = new ArrayList<>();

        NodeRef userPersonRef = authorityService.getAuthorityNodeRef(userName);
        if (userPersonRef != null && nodeService.exists(userPersonRef)) {
            NodeRef personalDocsRef = RepoUtils.getFirstTargetAssoc(userPersonRef,
                    PersonalDocumentsModel.TYPE_PERSONAL_DOCUMENTS,
                    nodeService);

            if (personalDocsRef != null && nodeService.exists(personalDocsRef)) {
                List<ChildAssociationRef> childDocs = nodeService.getChildAssocs(personalDocsRef);
                docs = RepoUtils.getChildNodeRefs(childDocs);
            }
        }

        return docs;
    }

    private void ensureAllPersonalDirectories() {
        Set<String> authorities = authorityService.getAllAuthoritiesInZone(AuthorityService.ZONE_APP_DEFAULT,
                AuthorityType.USER);

        for (String authorityName : authorities) {
            if (!skipPersons.contains(authorityName)) {
                ensureDirectory(authorityName);
            }
        }
    }

    private void createAssocToPersonalDocs(NodeRef userRef, NodeRef personalDocsFolder) {
        RepoUtils.createAssociation(userRef,
                personalDocsFolder,
                OrgStructModel.ASSOC_PERSONAL_DOCUMENTS,
                true,
                nodeService);
    }

    private void setPermissions(NodeRef folderRef, String userName) {
        permissionService.setInheritParentPermissions(folderRef, false);
        permissionService.setPermission(folderRef, userName, PermissionService.COORDINATOR, true);
        permissionService.setPermission(folderRef, GROUP_PERSONAL_DOCUMENTS_MANAGERS, PermissionService.COORDINATOR, true);
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setPersonalDocumentsRoot(LazyNodeRef personalDocumentsRoot) {
        this.personalDocumentsRoot = personalDocumentsRoot;
    }

    public void setSkipPersons(List<String> skipPersons) {
        this.skipPersons = skipPersons;
    }

}
