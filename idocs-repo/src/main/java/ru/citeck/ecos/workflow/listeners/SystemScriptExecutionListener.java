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
package ru.citeck.ecos.workflow.listeners;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.el.Expression;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.activiti.listener.ScriptExecutionListener;

public class SystemScriptExecutionListener extends ScriptExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception 
    {
    	super.setRunAs(new StringExpression(AuthenticationUtil.getAdminUserName()));
        super.notify(execution);
    }
    
}

class StringExpression implements Expression {

	private String value;
	
	public StringExpression(String value) {
		this.value = value;
	}
	
	@Override
	public String getExpressionText() {
		return value;
	}

	@Override
	public Object getValue(VariableScope arg0) {
		return value;
	}

	@Override
	public void setValue(Object arg0, VariableScope arg1) {
		
	}
	
}

