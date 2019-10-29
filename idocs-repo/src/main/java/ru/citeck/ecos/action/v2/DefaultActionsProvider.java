package ru.citeck.ecos.action.v2;

import com.fasterxml.jackson.databind.JsonNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.apps.app.module.type.type.action.ActionDto;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provide default actions for node.
 *
 * @author Roman Makarskiy
 */
@Component
public class DefaultActionsProvider implements NodeActionsV2Provider {

    private static final String HAS_CONTENT_PATTERN = ".has(n:\"_content\")";

    private static final String EDIT_KEY = "dao.edit";
    private static final String VIEW_KEY = "dto.view";
    private static final String DOWNLOAD_KEY = "dto.download";
    private static final String DELETE_KEY = "dto.delete";

    private static final List<ActionDto> ACTIONS;

    static {
        ActionDto view = new ActionDto();
        view.setType("view");
        view.setKey(VIEW_KEY);

        ActionDto edit = new ActionDto();
        edit.setType("edit");
        edit.setKey(EDIT_KEY);

        ActionDto delete = new ActionDto();
        delete.setType("delete");
        delete.setKey(DELETE_KEY);

        ActionDto download = new ActionDto();
        download.setType("download");
        download.setKey(DOWNLOAD_KEY);

        ACTIONS = Collections.unmodifiableList(Arrays.asList(view, edit, delete, download));
    }

    private final PermissionService permissionService;
    private final RecordsService recordsService;

    @Autowired
    public DefaultActionsProvider(PermissionService permissionService, RecordsService recordsService) {
        this.permissionService = permissionService;
        this.recordsService = recordsService;
    }

    @Override
    public List<ActionDto> getActions(NodeRef nodeRef) {
        return ACTIONS
                .stream()
                .filter(actionDto -> actionIsRequired(actionDto, nodeRef))
                .collect(Collectors.toList());
    }

    @Override
    public String getScope() {
        return "dto";
    }

    private boolean actionIsRequired(ActionDto action, NodeRef nodeRef) {
        if (nodeRef == null) {
            return false;
        }

        String key = action.getKey();

        switch (key) {
            case VIEW_KEY:
                return hasPermission(nodeRef, PermissionService.READ);
            case EDIT_KEY:
                return hasPermission(nodeRef, PermissionService.WRITE);
            case DELETE_KEY:
                return hasPermission(nodeRef, PermissionService.DELETE);
            case DOWNLOAD_KEY:
                return hasContent(nodeRef);
        }

        return false;
    }

    private boolean hasPermission(NodeRef nodeRef, String permission) {
        AccessStatus accessStatus = permissionService.hasPermission(nodeRef, permission);
        return AccessStatus.ALLOWED.equals(accessStatus);
    }

    private boolean hasContent(NodeRef nodeRef) {
        JsonNode result = recordsService.getAttribute(RecordRef.create("", nodeRef.toString()),
                HAS_CONTENT_PATTERN);
        return result.asBoolean();
    }
}
