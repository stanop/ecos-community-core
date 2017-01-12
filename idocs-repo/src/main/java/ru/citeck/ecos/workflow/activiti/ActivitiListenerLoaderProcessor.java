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
package ru.citeck.ecos.workflow.activiti;

import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;

import ru.citeck.ecos.spring.AbstractBeanProcessor;

public class ActivitiListenerLoaderProcessor extends AbstractBeanProcessor<ProcessEngineConfiguration> {

    private List<ActivitiListenerLoader> loaders;
    private Map<String, TaskListener> taskListeners;
    private Map<String, ExecutionListener> executionListeners;
    
    @Override
    public ProcessEngineConfiguration postProcessBeforeInitialization(ProcessEngineConfiguration config) {
        for(ActivitiListenerLoader loader : loaders) {
            if(loader.applies(config)) {
                loader.process(config, taskListeners, executionListeners);
            }
        }
        return config;
    }

    public void setLoaders(List<ActivitiListenerLoader> loaders) {
        this.loaders = loaders;
    }

    public void setTaskListeners(Map<String, TaskListener> taskListeners) {
        this.taskListeners = taskListeners;
    }

    public void setExecutionListeners(Map<String, ExecutionListener> executionListeners) {
        this.executionListeners = executionListeners;
    }

}
