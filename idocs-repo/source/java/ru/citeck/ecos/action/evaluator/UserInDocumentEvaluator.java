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
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Is user in document property evaluator
 *
 * @author Alexey Moiseyev
 */
public class UserInDocumentEvaluator extends ComparePropertyValueEvaluator {

    /**
     * Evaluator constants
     */
    public static final String NAME = "user-in-document-evaluator";
    public static final String PARAM_USER = "userName";

    PersonService personService;
    NamespaceService namespaceService;

    /**
     * @see org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase#evaluateImpl(org.alfresco.service.cmr.action.ActionCondition, org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean evaluateImpl(ActionCondition actionCondition, NodeRef actionedUponNodeRef) {
        String paramUserName = (String) actionCondition.getParameterValue(PARAM_USER);
        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();

        String userName = ((paramUserName != null) && (!paramUserName.isEmpty())) ? paramUserName : currentUserName;

        String property = (String) actionCondition.getParameterValue(PARAM_PROPERTY);

        if ((property != null) && (!property.isEmpty())) {
            QName propertyQName = QName.createQName(property, namespaceService);
            actionCondition.setParameterValue(PARAM_PROPERTY, propertyQName);

            // Set the operation to equals
            actionCondition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.EQUALS.name());

            // Set the userName
            actionCondition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, userName);

            return super.evaluateImpl(actionCondition, actionedUponNodeRef);
        }

        return false;
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_USER, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_USER), false));
        paramList.add(new ParameterDefinitionImpl(PARAM_PROPERTY, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_PROPERTY), false));
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

}
