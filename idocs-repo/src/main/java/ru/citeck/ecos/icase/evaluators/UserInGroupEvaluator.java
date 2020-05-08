package ru.citeck.ecos.icase.evaluators;

import lombok.Data;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records2.evaluator.RecordEvaluator;
import ru.citeck.ecos.records2.evaluator.RecordEvaluatorService;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserInGroupEvaluator implements RecordEvaluator<Object, Object, UserInGroupEvaluator.Config> {

    public static final String TYPE = "user-in-group";

    private RecordEvaluatorService recordEvaluatorService;
    private PersonService personService;
    private AuthorityService authorityService;

    @Autowired
    public UserInGroupEvaluator(ServiceRegistry serviceRegistry,
                                RecordEvaluatorService recordEvaluatorService) {
        this.personService = serviceRegistry.getPersonService();
        this.authorityService = serviceRegistry.getAuthorityService();
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
    public boolean evaluate(Object meta, Config config) {
        return AuthenticationUtil.runAsSystem(() -> {
            String username = getUsername(config);
            Set<String> groupNames = getGroups(config);

            if (StringUtils.isBlank(username) || CollectionUtils.isEmpty(groupNames)) {
                return false;
            }

            Set<String> userAuthorities = authorityService.getAuthoritiesForUser(username);
            if (CollectionUtils.isEmpty(userAuthorities)) {
                return false;
            }

            return CollectionUtils.containsAny(userAuthorities, groupNames);
        });
    }

    private String getUsername(Config config) {
        String username = config.userName;
        if (StringUtils.isBlank(username)) {
            username = AuthenticationUtil.getFullyAuthenticatedUser();
        }

        if (personService.personExists(username)) {
            return username;
        }
        return null;
    }

    private Set<String> getGroups(Config config) {
        if (StringUtils.isBlank(config.groupName)) {
            return Collections.emptySet();
        }

        String[] splitedGroups = config.groupName.split(",");
        return Arrays.stream(splitedGroups)
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    @Data
    public static class Config {
        private String userName;
        private String groupName;
    }
}
