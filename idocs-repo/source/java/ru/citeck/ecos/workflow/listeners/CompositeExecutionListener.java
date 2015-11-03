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
import org.activiti.engine.delegate.ExecutionListener;

import java.util.List;

public class CompositeExecutionListener implements ExecutionListener {

    private List<ExecutionListener> listeners;

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        for(ExecutionListener listener : listeners) {
            listener.notify(delegateExecution);
        }
    }

    public List<ExecutionListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<ExecutionListener> listeners) {
        this.listeners = listeners;
    }
}
