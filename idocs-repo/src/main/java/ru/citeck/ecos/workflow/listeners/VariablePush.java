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

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;

/**
 * This task listener automatically copies specified variable to execution context.
 * 
 * @author Sergey Tiunov
 */
public class VariablePush implements TaskListener 
{

    private String executionVariable;
    private String taskVariable;
    private String variable;
    private boolean ifNotNull = false;

    @Override
    public void notify(DelegateTask task) {
        
        Object value = task.getVariable(taskVariable != null ? taskVariable : variable);
        if(!ifNotNull || value != null) {
            task.getExecution().setVariable(executionVariable != null ? executionVariable : variable, value);
        }
        
    }

	public void setExecutionVariable(String executionVariable) {
		this.executionVariable = executionVariable;
	}

	public void setTaskVariable(String taskVariable) {
		this.taskVariable = taskVariable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

    public void setIfNotNull(boolean ifNotNull) {
        this.ifNotNull = ifNotNull;
    }

    public void setExecutionVariableExpr(Expression executionVariable) {
        this.executionVariable = executionVariable.getExpressionText();
    }
    
    public void setTaskVariableExpr(Expression taskVariable) {
        this.taskVariable = taskVariable.getExpressionText();
    }
    
    public void setVariableExpr(Expression variable) {
        this.variable = variable.getExpressionText();
    }

    public void setIfNotNullExpr(Expression ifNotNull) {
        this.ifNotNull = Boolean.valueOf(ifNotNull.getExpressionText());
    }

}
