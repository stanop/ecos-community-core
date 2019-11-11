package ru.citeck.ecos.comment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.comment.model.CommentDTO;
import ru.citeck.ecos.comment.model.CommentPermissions;
import ru.citeck.ecos.records.models.AuthorityDTO;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Roman Makarskiy
 */
@Component
public class CommentFactory {

    private static final int EDITED_DIFF_RANGE = 100;
    private static final String SITE_MANAGER = "SiteManager";

    private final LockService lockService;
    private final PermissionService permissionService;
    private final ContentService contentService;
    private final NodeService nodeService;
    private final RecordsService recordsService;
    private final AuthorityService authorityService;
    private final ServiceRegistry serviceRegistry;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public CommentFactory(LockService lockService, RecordsService recordsService,
                          PermissionService permissionService, ContentService contentService, NodeService nodeService,
                          AuthorityService authorityService,
                          ServiceRegistry serviceRegistry) {
        this.lockService = lockService;
        this.permissionService = permissionService;
        this.contentService = contentService;
        this.nodeService = nodeService;
        this.recordsService = recordsService;
        this.authorityService = authorityService;
        this.serviceRegistry = serviceRegistry;
    }

    public CommentDTO fromNode(NodeRef commentRef) {
        CommentDTO dto = new CommentDTO();

        Map<QName, Serializable> properties = nodeService.getProperties(commentRef);

        Date createdAt = (Date) properties.get(ContentModel.PROP_CREATED);
        Date modifiedAt = (Date) properties.get(ContentModel.PROP_MODIFIED);

        dto.setCreatedAt(createdAt);
        dto.setModifiedAt(modifiedAt);
        dto.setEdited(isEdited(createdAt, modifiedAt));
        dto.setAuthor(toUserDTO((String) properties.get(ContentModel.PROP_CREATOR)));
        dto.setEditor(toUserDTO((String) properties.get(ContentModel.PROP_MODIFIER)));

        dto.setText(getCommentText(commentRef));
        dto.setId(commentRef.getId());
        dto.setPermissions(getPermissions(commentRef));

        return dto;
    }

    private Boolean isEdited(Date createdAt, Date modifiedAt) {
        if (createdAt == null || modifiedAt == null) {
            return false;
        }

        long diff = modifiedAt.getTime() - createdAt.getTime();
        return diff >= EDITED_DIFF_RANGE;
    }

    private AuthorityDTO toUserDTO(String userName) {
        NodeRef userRef = authorityService.getAuthorityNodeRef(userName);
        RecordRef userRecord = RecordRef.create("", userRef.toString());
        return recordsService.getMeta(userRecord, AuthorityDTO.class);
    }

    private String getCommentText(NodeRef commentRef) {
        ContentReader reader = contentService.getReader(commentRef, ContentModel.PROP_CONTENT);
        return reader != null ? reader.getContentString() : null;
    }


    private JsonNode getPermissions(NodeRef commentRef) {
        boolean canEdit;
        boolean canDelete;
        boolean isNodeLocked = false;

        Set<QName> aspects = nodeService.getAspects(commentRef);

        boolean isWorkingCopy = aspects.contains(ContentModel.ASPECT_WORKING_COPY);
        if (!isWorkingCopy) {
            if (aspects.contains(ContentModel.ASPECT_LOCKABLE)) {
                LockStatus lockStatus = lockService.getLockStatus(commentRef);
                if (lockStatus == LockStatus.LOCKED || lockStatus == LockStatus.LOCK_OWNER) {
                    isNodeLocked = true;
                }
            }
        }

        if (isNodeLocked || isWorkingCopy) {
            canEdit = false;
            canDelete = false;
        } else {
            boolean canAccessAsSiteManager =
                    permissionService.hasPermission(commentRef, SITE_MANAGER) == AccessStatus.ALLOWED;
            boolean canAccessAsCoordinator =
                    permissionService.hasPermission(commentRef, PermissionService.COORDINATOR) == AccessStatus.ALLOWED;
            String author = (String) nodeService.getProperties(commentRef).get(ContentModel.PROP_CREATOR);
            String currentUser = serviceRegistry.getAuthenticationService().getCurrentUserName();
            canEdit = author.equals(currentUser) || canAccessAsSiteManager || canAccessAsCoordinator;
            canDelete = permissionService.hasPermission(commentRef, PermissionService.DELETE) == AccessStatus.ALLOWED;
        }

        Map<String, Boolean> permissions = new HashMap<>();
        permissions.put(CommentPermissions.CAN_EDIT.getValue(), canEdit);
        permissions.put(CommentPermissions.CAN_DELETE.getValue(), canDelete);

        return mapper.convertValue(permissions, JsonNode.class);
    }

}
