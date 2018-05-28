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

import java.util.List;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import ru.citeck.ecos.utils.performance.ActionPerformance;
import ru.citeck.ecos.utils.performance.Performance;

/**
 * Composite Activiti TaskListener.
 * 
 * @author Sergey Tiunov
 *
 */
public class CompositeTaskListener implements TaskListener {

    private List<TaskListener> listeners;

    @Override
    public void notify(DelegateTask task) {
        for (TaskListener listener : listeners) {
            String actionKey;
            if (task != null) {
                actionKey = String.format("taskId: %s event: %s", task.getId(), task.getEventName());
            } else {
                actionKey = "Notify with unknown task";
            }
            Performance perf = new ActionPerformance(listener, actionKey);
            listener.notify(task);
            perf.stop();
        }
    }

    public void setListeners(List<TaskListener> listeners) {
        this.listeners = listeners;
    }

}
