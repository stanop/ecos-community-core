package ru.citeck.ecos.workflow.activiti;

import java.lang.reflect.*;
import java.util.Map;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;

import ru.citeck.ecos.utils.SimpleInvocationHandler;
import ru.citeck.ecos.workflow.activiti.ActivitiListenerLoader;

class ActivitiListenerLoaderListenerImpl implements ActivitiListenerLoader
{
    @Override
    boolean applies(ProcessEngineConfiguration config) {
        return config.metaClass.respondsTo(config, "getCustomPreBPMNParseListeners");
    }
    
    @Override
    void process(ProcessEngineConfiguration config, Map<String, TaskListener> taskListeners, Map<String, ExecutionListener> executionListeners) {
        List parseListeners = config.getCustomPreBPMNParseListeners();
        if(parseListeners == null) {
            throw new IllegalStateException("We need to load activiti listeners after engine's properties are set");
        }
        parseListeners.add(createListener(taskListeners, executionListeners));
    }

    Object createListener(Map<String, TaskListener> taskListeners, Map<String, ExecutionListener> executionListeners) {
        Class BpmnClassListener_class = Class.forName("org.activiti.engine.impl.bpmn.parser.BpmnParseListener");
        Class UserTaskActivityBehavior_class = Class.forName("org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior");
        return Proxy.newProxyInstance(
            BpmnClassListener_class.getClassLoader(),
            [ BpmnClassListener_class ] as Class[],
            new SimpleInvocationHandler() {
                
                void parseUserTask(Object element, Object scope, Object activity) {
                    Object activitybehaviour = activity.getActivityBehavior();
                    if (UserTaskActivityBehavior_class.isInstance(activitybehaviour)) {
                        for(String eventName : taskListeners.keySet()) {
                            TaskListener listener = taskListeners.get(eventName);
                            if(listener != null) {
                                activitybehaviour.getTaskDefinition().addTaskListener(eventName, listener);
                            }
                        }
                    }
                }

                void parseRootElement(Object element, List processDefinitionEntities) {
                    for (Object processDefinition : processDefinitionEntities)
                    {
                        for(String eventName : executionListeners.keySet()) {
                            ExecutionListener listener = executionListeners.get(eventName);
                            if(listener != null) {
                                processDefinition.addExecutionListener(eventName, listener);
                            }
                        }
                    }
                }
                
            }
        );
    }
}