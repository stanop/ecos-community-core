package ru.citeck.ecos.action;

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

//TODO: Rewrite check permission/content with evaluators?

/**
 * Provide default actions for node.
 *
 * @author Roman Makarskiy
 */
@Component
public class DefaultActionsProvider {

    private static final String HAS_CONTENT_PATTERN = ".has(n:\"_content\")";

    private static final String EDIT_KEY = "dao.edit";
    private static final String VIEW_KEY = "dto.view";
    private static final String DOWNLOAD_KEY = "dto.download";
    private static final String DELETE_KEY = "dto.delete";

    private static final List<ActionDto> ACTIONS;

    static {
        ActionDto view = new ActionDto();
        view.setName("grid.inline-tools.show");
        view.setIcon("icon-on");
        view.setType("view");
        view.setKey(VIEW_KEY);

        ActionDto edit = new ActionDto();
        edit.setName("grid.inline-tools.edit");
        edit.setIcon("icon-edit");
        edit.setType("edit");
        edit.setKey(EDIT_KEY);

        ActionDto delete = new ActionDto();
        delete.setName("grid.inline-tools.delete");
        delete.setIcon("icon-delete");
        delete.setType("delete");
        delete.setKey(DELETE_KEY);

        ActionDto download = new ActionDto();
        download.setName("grid.inline-tools.download");
        download.setIcon("icon-download");
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

    public List<ActionDto> getDefaultActions(NodeRef nodeRef) {
        return ACTIONS
                .stream()
                .filter(actionDto -> actionIsRequired(actionDto, nodeRef))
                .collect(Collectors.toList());
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
