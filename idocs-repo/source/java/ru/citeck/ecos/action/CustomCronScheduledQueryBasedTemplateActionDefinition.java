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
package ru.citeck.ecos.action;


import org.alfresco.repo.action.scheduled.CronScheduledQueryBasedTemplateActionDefinition;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;


import java.util.Map;
import java.util.Properties;

public class CustomCronScheduledQueryBasedTemplateActionDefinition extends CronScheduledQueryBasedTemplateActionDefinition {
    private static Logger logger = Logger.getLogger(CustomCronScheduledQueryBasedTemplateActionDefinition.class);
    private boolean enabled = false;
    private Map<String, String> params;
    private Properties globalProperties;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }


    @Override
    public Action getAction(NodeRef nodeRef) {
        // Use the template to build its action
        Action action = super.getAction(nodeRef);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            action.setParameterValue(entry.getKey(), entry.getValue());
        }
        return action;
    }

    public Properties getGlobalProperties() {
        return globalProperties;
    }

    public void setGlobalProperties(Properties globalProperties) {
        this.globalProperties = globalProperties;
    }

    @Override
    public void register(Scheduler scheduler) throws SchedulerException {
       String val =  (String)globalProperties.get("job.replace-docs-to-archive.enabled");
        enabled = Boolean.parseBoolean(val);
        if ((scheduler == null) || (!enabled)) {
            logger.warn("Job " + getJobName() + " is not active/enabled");
        } else {
            super.register(scheduler);
        }
    }
}
