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
package ru.citeck.ecos.processor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.workflow.*;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

public class BuildWorkflowModel extends AbstractDataBundleLine
{
    private WorkflowService workflowService;
    private String workflowIdExpr;
    private String workflowModelKey;
    private String tasksModelKey;
    private String pathsModelKey;
    private String propsModelKey;
    private NamespacePrefixResolver namespaceService;

    public void init() {
        this.workflowService = this.serviceRegistry.getWorkflowService();
        this.namespaceService = this.serviceRegistry.getNamespaceService();
    }

    @Override
    public DataBundle process(DataBundle input) {

        Map<String, Object> model = input.needModel();
        String workflowId = this.evaluateExpression(workflowIdExpr, model).toString();

        Map<String, Object> newModel = new HashMap<>(model.size() + 1);
        newModel.putAll(model);

        if(workflowModelKey != null) {
            newModel.put(workflowModelKey, getWorkflowModel(workflowId));
        }
        if(tasksModelKey != null) {
            newModel.put(tasksModelKey, getTasksModel(workflowId));
        }
        if(pathsModelKey != null) {
            newModel.put(pathsModelKey, getPathsModel(workflowId));
        }
        if(propsModelKey != null) {
            newModel.put(propsModelKey, getPropsModel(workflowId));
        }

        return new DataBundle(input, newModel);
    }

    private Object getWorkflowModel(String workflowId) {
        if(workflowId == null) {
            return null;
        }
        return workflowService.getWorkflowById(workflowId);
    }

    private Object getTasksModel(String workflowId) {
        WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
        taskQuery.setProcessId(workflowId);
        taskQuery.setActive(null);
        taskQuery.setTaskState(null);

        // backwards compatibility with 4.0.c
        @SuppressWarnings("deprecation")
        List<WorkflowTask> tasks = workflowService.queryTasks(taskQuery);

        return tasks;
    }

    private Object getPathsModel(String workflowId) {
        return workflowService.getWorkflowPaths(workflowId);
    }

    private Object getPropsModel(String workflowId) {
        List<WorkflowPath> paths = workflowService.getWorkflowPaths(workflowId);
        Map<String,Object> props = new HashMap<>();
        for (WorkflowPath path : paths) {
            Map<QName, Serializable> pathProps = workflowService.getPathProperties(path.getId());
            for(Map.Entry<QName, Serializable> propEntry : pathProps.entrySet()) {
                QName propQName = propEntry.getKey();
                String propShortName = propQName.toPrefixString(namespaceService);
                String propFullName = propQName.toString();

                Serializable pathProp = propEntry.getValue();
                props.put(propShortName, pathProp);
                props.put(propFullName, pathProp);
            }
        }
        return props;
    }

    public void setWorkflowId(String workflowIdExpr) {
        this.workflowIdExpr = workflowIdExpr;
    }

    public String getWorkflowModelKey() {
        return workflowModelKey;
    }

    public void setWorkflowModelKey(String workflowModelKey) {
        this.workflowModelKey = workflowModelKey;
    }

    public void setTasksModelKey(String tasksModelKey) {
        this.tasksModelKey = tasksModelKey;
    }

    public void setPathsModelKey(String pathsModelKey) {
        this.pathsModelKey = pathsModelKey;
    }

    public void setPropsModelKey(String propsModelKey) {
        this.propsModelKey = propsModelKey;
    }

}
