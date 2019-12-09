package ru.citeck.ecos.action.v2;

import com.fasterxml.jackson.databind.JsonNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.apps.app.module.type.ui.action.ActionModule;
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
//@Component
public class DefaultActionsProvider implements NodeActionsV2Provider {

    private static final String HAS_CONTENT_PATTERN = ".has(n:\"_content\")";

    private static final String EDIT_KEY = "dao.edit";
    private static final String VIEW_KEY = "dao.view";
    private static final String DOWNLOAD_KEY = "dao.download";
    private static final String DELETE_KEY = "dao.delete";

    private static final List<ActionModule> ACTIONS;

    static {
        ActionModule view = new ActionModule();
        view.setType("view");
        view.setKey(VIEW_KEY);

        ActionModule edit = new ActionModule();
        edit.setType("edit");
        edit.setKey(EDIT_KEY);

        ActionModule delete = new ActionModule();
        delete.setType("delete");
        delete.setKey(DELETE_KEY);

        ActionModule download = new ActionModule();
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
    public List<ActionModule> getActions(NodeRef nodeRef) {
        return ACTIONS
                .stream()
                .filter(ActionModule -> actionIsRequired(ActionModule, nodeRef))
                .collect(Collectors.toList());
    }

    @Override
    public String getScope() {
        return "dto";
    }

    private boolean actionIsRequired(ActionModule action, NodeRef nodeRef) {
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
