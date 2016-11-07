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
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.task.IdentityLinkType;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import ru.citeck.ecos.deputy.AvailabilityServiceImpl;
import ru.citeck.ecos.deputy.TaskDeputyListener;

import java.util.ArrayList;

/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class TaskOriginalOwnerListener implements TaskListener, ApplicationContextAware {

    private AvailabilityServiceImpl availabilityService;
    private ApplicationContext applicationContext;

    // NOTE: we have to import delegate listener by name
    //  to resolve circular dependency
    private String delegateListenerName;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setDelegateListenerName(String delegateListenerName) {
        this.delegateListenerName = delegateListenerName;
    }

    public void setAvailabilityService(AvailabilityServiceImpl availabilityService) {
        this.availabilityService = availabilityService;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        Object originalOwner = delegateTask.getVariableLocal("taskOriginalOwner");
        String assignee = delegateTask.getAssignee();
        if (originalOwner == null) {
            delegateTask.setVariableLocal("taskOriginalOwner", assignee);
        }

        if (assignee != null) {
            TaskDeputyListener delegateListener = applicationContext.getBean(delegateListenerName, TaskDeputyListener.class);
            ArrayList<String> actorsList = delegateListener.getActorsList(assignee);
            for (String actor : actorsList) {
                delegateTask.addUserIdentityLink(actor, IdentityLinkType.CANDIDATE);
            }
            delegateTask.setAssignee(null);
        }
    }
}
