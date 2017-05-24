package ru.citeck.ecos.action.evaluator;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import ru.citeck.ecos.role.CaseRoleService;

import java.util.List;

/**
 * @author Maxim Strizhov (maxim.strizhov@citeck.ru)
 */
public class UserInRolesEvaluator extends ActionConditionEvaluatorAbstractBase {
    /**
     * Evaluator constants
     */
    public static final String NAME = "user-in-roles";
    public static final String PARAM_ROLES = "roles";

    private CaseRoleService caseRoleService;
    private PersonService personService;

    @Override
    protected boolean evaluateImpl(ActionCondition actionCondition, NodeRef nodeRef) {
        String rolesParamValue = (String) actionCondition.getParameterValue(PARAM_ROLES);
        if (rolesParamValue != null && !rolesParamValue.isEmpty()) {
            String[] roles = rolesParamValue.split(",");
            String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
            if (currentUserName != null && personService.personExists(currentUserName)) {
                NodeRef personRef = personService.getPerson(currentUserName);
                if (personRef != null) {
                    boolean userInRoles = false;
                    for (String role : roles) {
                        userInRoles = userInRoles || caseRoleService.isRoleMember(nodeRef, role, personRef, false);
                    }
                    return userInRoles;
                }
            }
        }
        return false;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_ROLES, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_ROLES), false));
    }

    public void setCaseRoleService(CaseRoleService caseRoleService) {
        this.caseRoleService = caseRoleService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }
}
