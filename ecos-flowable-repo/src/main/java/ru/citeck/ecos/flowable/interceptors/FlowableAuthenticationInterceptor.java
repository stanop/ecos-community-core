package ru.citeck.ecos.flowable.interceptors;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.lang.StringUtils;
import org.flowable.common.engine.impl.interceptor.AbstractCommandInterceptor;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandConfig;

/**
 * @author Roman Makarskiy
 */
public class FlowableAuthenticationInterceptor extends AbstractCommandInterceptor {
    @Override
    public <T> T execute(CommandConfig config, Command<T> command) {
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        if (StringUtils.isBlank(currentUser)) {
            return AuthenticationUtil.runAsSystem(() -> next.execute(config, command));
        } else {
            return next.execute(config, command);
        }
    }
}