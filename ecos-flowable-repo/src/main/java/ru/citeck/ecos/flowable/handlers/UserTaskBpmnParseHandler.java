package ru.citeck.ecos.flowable.handlers;

import org.flowable.bpmn.model.BaseElement;
import org.flowable.bpmn.model.FlowableListener;
import org.flowable.bpmn.model.ImplementationType;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.impl.bpmn.parser.BpmnParse;
import org.flowable.engine.parse.BpmnParseHandler;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.flowable.listeners.global.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * User task bpmn parse handler. Add global task listeners to every user tasks
 */
@Component
public class UserTaskBpmnParseHandler implements BpmnParseHandler, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public Collection<Class<? extends BaseElement>> getHandledTypes() {
        List<Class<? extends BaseElement>> result = new ArrayList<>();
        result.add(UserTask.class);
        return result;
    }

    @Override
    public void parse(BpmnParse bpmnParse, BaseElement baseElement) {
        UserTask userTask = (UserTask) baseElement;
        List<FlowableListener> listeners = userTask.getTaskListeners();

        /* All */
        Collection<String> allTaskListeners = getBeansNames(GlobalAllTaskListener.class);
        for (String taskListener : allTaskListeners) {
            listeners.add(createFlowableListener(taskListener, TaskListener.EVENTNAME_ALL_EVENTS));
        }

        /* Assignment */
        Collection<String> assignmentTaskListeners = getBeansNames(GlobalAssignmentTaskListener.class);
        for (String taskListener : assignmentTaskListeners) {
            listeners.add(createFlowableListener(taskListener, TaskListener.EVENTNAME_ASSIGNMENT));
        }

        /* Complete */
        Collection<String> completeTaskListeners = getBeansNames(GlobalCompleteTaskListener.class);
        for (String taskListener : completeTaskListeners) {
            listeners.add(createFlowableListener(taskListener, TaskListener.EVENTNAME_COMPLETE));
        }

        /* Create */
        Collection<String> createTaskListeners = getBeansNames(GlobalCreateTaskListener.class);
        for (String taskListener : createTaskListeners) {
            listeners.add(createFlowableListener(taskListener, TaskListener.EVENTNAME_CREATE));
        }

        /* Delete */
        Collection<String> deleteTaskListeners = getBeansNames(GlobalDeleteTaskListener.class);
        for (String taskListener : deleteTaskListeners) {
            listeners.add(createFlowableListener(taskListener, TaskListener.EVENTNAME_DELETE));
        }

        userTask.setTaskListeners(listeners);
    }

    private List<String> getBeansNames(Class<?> beansClass) {
        return Arrays.asList(applicationContext.getBeanNamesForType(beansClass));
    }

    private FlowableListener createFlowableListener(String taskListener, String event) {
        FlowableListener result = new FlowableListener();
        result.setEvent(event);
        result.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
        result.setImplementation("${" + taskListener + "}");
        return result;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
