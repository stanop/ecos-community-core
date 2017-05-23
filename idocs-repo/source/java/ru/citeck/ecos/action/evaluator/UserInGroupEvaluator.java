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
import java.util.Set;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;

/**
 * Is user in group evaluator
 *
 * @author Alexey Moiseyev
 */
public class UserInGroupEvaluator extends ActionConditionEvaluatorAbstractBase {

    /**
     * Evaluator constants
     */
    public static final String NAME = "user-in-group";
    public static final String PARAM_USER = "userName";
    public static final String PARAM_GROUP = "groupName";

    PersonService personService;
    AuthorityService authorityService;

    /**
     * @see org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase#evaluateImpl(org.alfresco.service.cmr.action.ActionCondition, org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean evaluateImpl(ActionCondition actionCondition, NodeRef actionedUponNodeRef) {
        String paramUserName = (String) actionCondition.getParameterValue(PARAM_USER);
        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();

        final String userName = ((paramUserName != null) && (!paramUserName.isEmpty())) ? paramUserName : currentUserName;
        final String groupName = (String) actionCondition.getParameterValue(PARAM_GROUP);

        return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Boolean>() {
            @Override
            public Boolean doWork() throws Exception {
                if ((userName != null) && (groupName != null)) {
                    String[] groupNames = groupName.split(",");
                    NodeRef personRef = personService.personExists(userName) ? personService.getPerson(userName) : null;

                    if (personRef != null) {
                        Set<String> userAuthorities = authorityService.getAuthoritiesForUser(userName);
                        boolean isUserInGroup = false;
                        if (userAuthorities != null) {
                            for (String groupName : groupNames) {
                                isUserInGroup = isUserInGroup || userAuthorities.contains(groupName.trim());
                            }
                            return isUserInGroup;
                        }
//                        if ((userAuthorities != null) && (userAuthorities.contains(groupName)))
//                            return true;
                    }
                }

                return false;
            }
        });

    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_USER, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_USER), false));
        paramList.add(new ParameterDefinitionImpl(PARAM_GROUP, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_GROUP), false));
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

}
