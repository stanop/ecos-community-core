package ru.citeck.ecos.behavior.authority;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.EcosModel;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class DisablingPersonBehaviour implements NodeServicePolicies.OnUpdatePropertiesPolicy {

    private static Log logger = LogFactory.getLog(DisablingPersonBehaviour.class);

    private PolicyComponent policyComponent;

    private AuthorityService authorityService;
    private MutableAuthenticationService authenticationService;

    private int order = 80;

    public void init() {
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                ContentModel.TYPE_PERSON,
                new OrderedBehaviour(this,
                        "onUpdateProperties",
                        Behaviour.NotificationFrequency.EVERY_EVENT,
                        order)
        );
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        Serializable isPersonDisabledBefore = before.get(EcosModel.PROP_IS_PERSON_DISABLED);
        Serializable isPersonDisabledAfter = after.get(EcosModel.PROP_IS_PERSON_DISABLED);

        if (Objects.equals(isPersonDisabledBefore, isPersonDisabledAfter)) {
            return;
        }

        String changedUserName = (String) after.get(ContentModel.PROP_USERNAME);

        if (!authenticationService.isAuthenticationMutable(changedUserName)) {
            return;
        }

        if (isPersonDisabledBefore == null) {
            String currentAuthUser = AuthenticationUtil.getFullyAuthenticatedUser();
            if (authorityService.isAdminAuthority(currentAuthUser) && isPersonDisabledAfter != null) {
                authenticationService.setAuthenticationEnabled(changedUserName,
                        Boolean.FALSE.equals(isPersonDisabledAfter));
            }
        } else if (isPersonDisabledAfter != null) {
            authenticationService.setAuthenticationEnabled(changedUserName,
                    Boolean.FALSE.equals(isPersonDisabledAfter));
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setAuthenticationService(MutableAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
}