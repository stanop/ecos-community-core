/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.action.evaluator;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;

/**
 * Is user has permission evaluator
 *
 * @author Alexey Moiseyev
 */
public class UserHasPermissionEvaluator extends ActionConditionEvaluatorAbstractBase {

    /**
     * Evaluator constants
     */
    public static final String NAME = "user-has-permission-evaluator";
    public static final String PARAM_USER = "userName";
    public static final String PARAM_PERMISSION = "permission";

    PersonService personService;
    PermissionService permissionService;

    /**
     * @see org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase#evaluateImpl(org.alfresco.service.cmr.action.ActionCondition, org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean evaluateImpl(ActionCondition actionCondition, final NodeRef actionedUponNodeRef) {
        final String paramUserName = (String) actionCondition.getParameterValue(PARAM_USER);
        final String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
        final String permission = (String) actionCondition.getParameterValue(PARAM_PERMISSION);

        if ((permission != null) && (!permission.isEmpty())) {
            boolean isCorrectUserNameGiven = false;
            if (paramUserName != null) {
                isCorrectUserNameGiven = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Boolean>() {
                    @Override
                    public Boolean doWork() throws Exception {
                        return personService.personExists(paramUserName);
                    }
                });
            }

            if (isCorrectUserNameGiven && !paramUserName.equals(currentUserName)) {
                return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Boolean>() {
                    @Override
                    public Boolean doWork() throws Exception {
                        AccessStatus as = permissionService.hasPermission(actionedUponNodeRef, permission);
                        return AccessStatus.ALLOWED.equals(as);
                    }
                }, paramUserName);
            } else if ((paramUserName == null) || (paramUserName.equals(currentUserName))) {
                AccessStatus as = permissionService.hasPermission(actionedUponNodeRef, permission);
                return AccessStatus.ALLOWED.equals(as);
            }
        }

        return false;
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_USER, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_USER), false));
        paramList.add(new ParameterDefinitionImpl(PARAM_PERMISSION, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_PERMISSION), false));
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

}
