package ru.citeck.ecos.action.evaluator;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.site.SiteService;

import java.util.List;

public class SiteMemberEvaluator extends ActionConditionEvaluatorAbstractBase {

    public static final String NAME = "site-member";
    public static final String PARAM_AUTHORITY = "authorityName";
    public static final String PARAM_SITE = "siteName";

    private SiteService siteService;
    private AuthorityService authorityService;

    /**
     * @see org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase#evaluateImpl(org.alfresco.service.cmr.action.ActionCondition, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected boolean evaluateImpl(ActionCondition actionCondition, NodeRef actionedUponNodeRef) {
        String paramAuthorityName = (String) actionCondition.getParameterValue(PARAM_AUTHORITY);

        if (paramAuthorityName == null || paramAuthorityName.isEmpty()) {
            paramAuthorityName = AuthenticationUtil.getFullyAuthenticatedUser();
        }

        final String authorityName = paramAuthorityName;
        final String siteName = (String) actionCondition.getParameterValue(PARAM_SITE);

        return AuthenticationUtil.runAsSystem(() -> {
            if (siteName == null || !siteService.hasSite(siteName) || !authorityService.authorityExists(authorityName)) {
                return false;
            }
            return siteService.isMember(siteName, authorityName);
        });
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_AUTHORITY, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_AUTHORITY), false));
        paramList.add(new ParameterDefinitionImpl(PARAM_SITE, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_SITE), false));
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }
}
