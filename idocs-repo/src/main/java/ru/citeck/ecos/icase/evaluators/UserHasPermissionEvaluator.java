package ru.citeck.ecos.icase.evaluators;

import lombok.Data;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records2.RecordMeta;
import ru.citeck.ecos.records2.evaluator.RecordEvaluator;
import ru.citeck.ecos.records2.evaluator.RecordEvaluatorService;

import javax.annotation.PostConstruct;

@Component
public class UserHasPermissionEvaluator implements
        RecordEvaluator<Object, RecordMeta, UserHasPermissionEvaluator.Config> {

    public static final String TYPE = "user-has-permission";

    private RecordEvaluatorService recordEvaluatorService;
    private PersonService personService;
    private PermissionService permissionService;

    @Autowired
    public UserHasPermissionEvaluator(ServiceRegistry serviceRegistry,
                                      RecordEvaluatorService recordEvaluatorService) {
        this.personService = serviceRegistry.getPersonService();
        this.permissionService = serviceRegistry.getPermissionService();
        this.recordEvaluatorService = recordEvaluatorService;
    }

    @PostConstruct
    public void init() {
        recordEvaluatorService.register(this);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Object getMetaToRequest(Config config) {
        return null;
    }

    @Override
    public boolean evaluate(RecordMeta meta, Config config) {
        NodeRef nodeRef = RecordsUtils.toNodeRef(meta.getId());
        String username = getUsername(config);
        String permission = config.permission;

        if (StringUtils.isBlank(username) || StringUtils.isBlank(permission)) {
            return false;
        }

        return AuthenticationUtil.runAs(() -> {
            AccessStatus accessStatus = permissionService.hasPermission(nodeRef, permission);
            return AccessStatus.ALLOWED == accessStatus;
        }, username);
    }

    private String getUsername(Config config) {
        return AuthenticationUtil.runAsSystem(() -> {
            String username = config.username;
            if (StringUtils.isBlank(username)) {
                username = AuthenticationUtil.getFullyAuthenticatedUser();
            }

            if (personService.personExists(username)) {
                return username;
            }
            return null;
        });
    }

    @Data
    public static class Config {
        private String username;
        private String permission;
    }
}
