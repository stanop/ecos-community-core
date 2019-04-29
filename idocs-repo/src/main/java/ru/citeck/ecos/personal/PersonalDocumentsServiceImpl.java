package ru.citeck.ecos.personal;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.model.OrgStructModel;
import ru.citeck.ecos.model.PersonalDocumentsModel;
import ru.citeck.ecos.model.RequirementModel;
import ru.citeck.ecos.utils.LazyNodeRef;
import ru.citeck.ecos.utils.RepoUtils;
import ru.citeck.ecos.utils.TransactionUtils;

import java.util.ArrayList;
import java.util.List;

public class PersonalDocumentsServiceImpl extends AbstractLifecycleBean implements PersonalDocumentsService {

    private static final Log logger = LogFactory.getLog(PersonalDocumentsServiceImpl.class);

    private static final String GROUP_PERSONAL_DOCUMENTS_MANAGERS = "GROUP_personal_documents_managers";
    private static final String TEMP_DIR_NAME = "temp";

    private static final String ENSURE_DIRECTORY_FOR_USER = "Ensure 'personal documents' directory for user: ";

    private AuthorityService authorityService;
    private NodeService nodeService;
    private PermissionService permissionService;

    private LazyNodeRef personalDocumentsRoot;
    private List<NodeRef> checkLists;

    @Override
    protected void onBootstrap(ApplicationEvent event) {
        AuthenticationUtil.runAsSystem(() -> {
            ensureTempDirectory();
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

        if (logger.isDebugEnabled()) {
            logger.debug(ENSURE_DIRECTORY_FOR_USER + userName);
        }

        NodeRef userPersonRef = authorityService.getAuthorityNodeRef(userName);
        String userFullName = RepoUtils.getPersonFullName(userPersonRef, nodeService);
        NodeRef personalDocumentsFolderRef = null;

        if (userPersonRef != null && nodeService.exists(userPersonRef)) {

            NodeRef personalDocsRef = RepoUtils.getFirstTargetAssoc(userPersonRef,
                    PersonalDocumentsModel.TYPE_PERSONAL_DOCUMENTS,
                    nodeService);

            if (personalDocsRef == null || !nodeService.exists(personalDocsRef)) {
                NodeRef personalDocsRootRef = personalDocumentsRoot.getNodeRef();

                personalDocumentsFolderRef = nodeService.getChildByName(personalDocsRootRef,
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
                    ensureCheckLists(personalDocumentsFolderRef);
                    setPermissions(personalDocumentsFolderRef, userName);
                    addCaseAspect(personalDocumentsFolderRef);
                }
            }
        }
        return personalDocumentsFolderRef;
    }

    @Override
    public NodeRef ensureTempDirectory() {
        NodeRef tempPersonalDocsRootRef = null;
        NodeRef personalDocsRootRef = personalDocumentsRoot.getNodeRef();

        if (personalDocsRootRef != null && nodeService.exists(personalDocsRootRef)) {
            tempPersonalDocsRootRef = nodeService.getChildByName(personalDocsRootRef,
                    ContentModel.ASSOC_CONTAINS,
                    TEMP_DIR_NAME);

            if (tempPersonalDocsRootRef == null || !nodeService.exists(tempPersonalDocsRootRef)) {
                tempPersonalDocsRootRef = RepoUtils.createChildWithName(personalDocsRootRef,
                        ContentModel.ASSOC_CONTAINS,
                        PersonalDocumentsModel.TYPE_PERSONAL_DOCUMENTS,
                        TEMP_DIR_NAME,
                        nodeService);
            }

            addCaseAspect(tempPersonalDocsRootRef);
            ensureCheckLists(tempPersonalDocsRootRef);
        }
        return tempPersonalDocsRootRef;
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

    @Override
    public void ensureInPersonalFolder(NodeRef document, String userName) {
        ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(document);
        NodeRef parentRef = parentAssoc.getParentRef();
        QName parentAsscTypeQname = parentAssoc.getTypeQName();
        QName parentAsscQname = parentAssoc.getQName();

        NodeRef userFolder = ensureDirectory(userName);

        if (!parentRef.equals(userFolder)) {
            TransactionUtils.doAfterCommit(() -> {
                if (!nodeService.hasAspect(userFolder, ICaseModel.ASPECT_HAS_DOCUMENTS)) {
                    nodeService.addAspect(userFolder, ICaseModel.ASPECT_HAS_DOCUMENTS, null);
                }
                nodeService.moveNode(document, userFolder, parentAsscTypeQname, parentAsscQname);
            });
        }
    }

    private void createAssocToPersonalDocs(NodeRef userRef, NodeRef personalDocsFolder) {
        RepoUtils.createAssociation(userRef,
                personalDocsFolder,
                OrgStructModel.ASSOC_PERSONAL_DOCUMENTS,
                true,
                nodeService);
    }

    private void ensureCheckLists(NodeRef personalDocsFolder) {
        NodeRef atLeastOneCheckList = RepoUtils.getFirstTargetAssoc(personalDocsFolder,
                RequirementModel.ASSOC_COMPLETENESS_LEVELS,
                nodeService);
        if (atLeastOneCheckList == null && checkLists != null && checkLists.size() > 0) {
            for (NodeRef checkList : checkLists) {
                if (checkList != null && nodeService.exists(checkList)) {
                    RepoUtils.createAssociation(personalDocsFolder,
                            checkList,
                            RequirementModel.ASSOC_COMPLETENESS_LEVELS,
                            true,
                            nodeService);
                }
            }
        }
    }

    private void setPermissions(NodeRef folderRef, String userName) {
        permissionService.setInheritParentPermissions(folderRef, false);
        permissionService.setPermission(folderRef, userName, PermissionService.COORDINATOR, true);
        permissionService.setPermission(folderRef, GROUP_PERSONAL_DOCUMENTS_MANAGERS, PermissionService.COORDINATOR, true);
    }

    private void addCaseAspect(NodeRef personalDocsFolder) {
        nodeService.addAspect(personalDocsFolder, ICaseModel.ASPECT_CASE, null);
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

    public void setCheckLists(List<NodeRef> checkLists) {
        this.checkLists = checkLists;
    }
}
