package ru.citeck.ecos.cache.sync;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.ehcache.impl.internal.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.model.EcosCommonModel;
import ru.citeck.ecos.utils.NodeUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SyncKeysService {

    private static final NodeRef ROOT_REF = new NodeRef("workspace://SpacesStore/ecos-sync-keys-root");
    private static Boolean rootExists = null;

    private NodeUtils nodeUtils;
    private NodeService nodeService;
    private RepositoryState repositoryState;

    private final Map<String, NodeRef> nodeRefsByKey = new ConcurrentHashMap<>();

    @Autowired
    public SyncKeysService(NodeUtils nodeUtils,
                           NodeService nodeService) {

        this.nodeUtils = nodeUtils;
        this.nodeService = nodeService;
    }

    public long get(String key) {

        if (isServiceStateInvalid()) {
            return 0;
        }

        NodeRef keyRef = nodeRefsByKey.computeIfAbsent(key, this::searchKeyRef);
        if (keyRef == null) {
            return 0;
        }
        return AuthenticationUtil.runAsSystem(() -> {
            Date modifiedDate = nodeUtils.getProperty(keyRef, ContentModel.PROP_MODIFIED);
            return modifiedDate.getTime();
        });
    }

    public void update(String key) {

        if (isServiceStateInvalid()) {
            return;
        }

        NodeRef keyRef = nodeRefsByKey.computeIfAbsent(key, this::searchOrCreateKeyRef);
        if (keyRef != null) {
            nodeService.setProperty(keyRef, ContentModel.PROP_AUTHOR, System.currentTimeMillis());
        }
    }

    private boolean isServiceStateInvalid() {
        if (repositoryState.isBootstrapping()) {
            return false;
        }
        if (rootExists == null) {
            rootExists = nodeService.exists(ROOT_REF);
        }
        return !rootExists;
    }

    private NodeRef searchKeyRef(String key) {

        List<ChildAssociationRef> children =
                nodeService.getChildAssocsByPropertyValue(ROOT_REF, EcosCommonModel.PROP_KEY, key);

        if (children != null && !children.isEmpty()) {
            return children.get(0).getChildRef();
        }
        return null;
    }

    private NodeRef searchOrCreateKeyRef(String key) {

        NodeRef nodeRef = searchKeyRef(key);

        if (nodeRef == null) {
            Map<QName, Serializable> props = new HashMap<>();
            props.put(ContentModel.PROP_NAME, key);
            nodeRef = nodeUtils.createNode(ROOT_REF, ContentModel.TYPE_CMOBJECT, ContentModel.ASSOC_CHILDREN, props);
        }

        return nodeRef;
    }

    @Autowired
    @Qualifier("repositoryState")
    public void setRepositoryState(RepositoryState repositoryState) {
        this.repositoryState = repositoryState;
    }
}
