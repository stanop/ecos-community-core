package ru.citeck.ecos.workflow.activiti;

import java.lang.reflect.*;
import java.util.Map;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;

import ru.citeck.ecos.utils.SimpleInvocationHandler;

class ActivitiListenerLoaderHandlerImpl implements ActivitiListenerLoader
{
    @Override
    boolean applies(ProcessEngineConfiguration config) {
        return config.metaClass.respondsTo(config, "getPostBpmnParseHandlers");
    }
    
    @Override
    void process(ProcessEngineConfiguration config, Map<String, TaskListener> taskListeners, Map<String, ExecutionListener> executionListeners) {
        List parseHandlers = config.getPostBpmnParseHandlers();
        if(parseHandlers == null) {
            throw new IllegalStateException("We need to load activiti listeners after engine's properties are set");
        }
        parseHandlers.add(createTaskHandler(taskListeners));
        parseHandlers.add(createProcessHandler(executionListeners));
    }

    Object createTaskHandler(Map<String, TaskListener> taskListeners) {
        Class BpmnClassListener_class = Class.forName("org.activiti.engine.parse.BpmnParseHandler");
        Class UserTaskActivityBehavior_class = Class.forName("org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior");
        return Proxy.newProxyInstance(
            BpmnClassListener_class.getClassLoader(),
            [ BpmnClassListener_class ] as Class[],
            new SimpleInvocationHandler() {
                Class elementClass = Class.forName("org.activiti.bpmn.model.UserTask");
                
                Collection getHandledTypes() {
                    return Collections.singletonList(elementClass);
                }
                
                void parse(Object bpmnParse, Object userTask)
                {
                    if(!elementClass.isInstance(userTask)) return;
                    Object activity = bpmnParse.getCurrentScope().findActivity(userTask.getId());
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
            
            }
        );
    }
    
    Object createProcessHandler(Map<String, ExecutionListener> executionListeners) {
        Class BpmnClassListener_class = Class.forName("org.activiti.engine.parse.BpmnParseHandler");
        return Proxy.newProxyInstance(
            BpmnClassListener_class.getClassLoader(),
            [ BpmnClassListener_class ] as Class[],
            new SimpleInvocationHandler() {
                Class elementClass = Class.forName("org.activiti.bpmn.model.Process");

                Collection getHandledTypes() {
                    return Collections.singletonList(elementClass);
                }
                
                void parse(Object bpmnParse, Object process)
                {
                    if(!elementClass.isInstance(process)) return;
                    Object processDefinition = bpmnParse.getCurrentProcessDefinition();
                    for(String eventName : executionListeners.keySet()) {
                        ExecutionListener listener = executionListeners.get(eventName);
                        if(listener != null) {
                            processDefinition.addExecutionListener(eventName, listener);
                        }
                    }
                }
            
            }
        );
    }
    
}