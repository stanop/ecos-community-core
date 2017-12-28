package ru.citeck.ecos.flowable.handlers;

import org.flowable.bpmn.model.BaseElement;
import org.flowable.bpmn.model.FlowableListener;
import org.flowable.bpmn.model.ImplementationType;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.impl.bpmn.parser.BpmnParse;
import org.flowable.engine.parse.BpmnParseHandler;
import ru.citeck.ecos.flowable.listeners.global.*;
import ru.citeck.ecos.providers.ApplicationContextProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User task bpmn parse handler. Add global task listeners to every user tasks
 */
public class UserTaskBpmnParseHandler implements BpmnParseHandler {

    /**
     * Get handler types
     * @return List of handled classes
     */
    @Override
    public Collection<Class<? extends BaseElement>> getHandledTypes() {
        List<Class<? extends BaseElement>> result = new ArrayList<>();
        result.add(UserTask.class);
        return result;
    }

    /**
     * Parse element
     * @param bpmnParse Bpmn parse
     * @param baseElement Parse element (process)
     */
    @Override
    public void parse(BpmnParse bpmnParse, BaseElement baseElement) {
        UserTask userTask = (UserTask) baseElement;
        List<FlowableListener> listeners = userTask.getTaskListeners();
        /** All */
        Collection<String> allTaskListeners = ApplicationContextProvider.getBeansNames(GlobalAllTaskListener.class);
        for (String taskListener : allTaskListeners) {
            listeners.add(createFlowableListener(taskListener, TaskListener.EVENTNAME_ALL_EVENTS));
        }
        /** Assignment */
        Collection<String> assignmentTaskListeners = ApplicationContextProvider.getBeansNames(GlobalAssignmentTaskListener.class);
        for (String taskListener : assignmentTaskListeners) {
            listeners.add(createFlowableListener(taskListener, TaskListener.EVENTNAME_ASSIGNMENT));
        }
        /** Complete */
        Collection<String> completeTaskListeners = ApplicationContextProvider.getBeansNames(GlobalCompleteTaskListener.class);
        for (String taskListener : completeTaskListeners) {
            listeners.add(createFlowableListener(taskListener, TaskListener.EVENTNAME_COMPLETE));
        }
        /** Create */
        Collection<String> createTaskListeners = ApplicationContextProvider.getBeansNames(GlobalCreateTaskListener.class);
        for (String taskListener : createTaskListeners) {
            listeners.add(createFlowableListener(taskListener, TaskListener.EVENTNAME_CREATE));
        }
        /** Delete */
        Collection<String> deleteTaskListeners = ApplicationContextProvider.getBeansNames(GlobalDeleteTaskListener.class);
        for (String taskListener : deleteTaskListeners) {
            listeners.add(createFlowableListener(taskListener, TaskListener.EVENTNAME_DELETE));
        }
        userTask.setTaskListeners(listeners);
    }

    /**
     * Create flowable listener
     * @param taskListener Task listener bean
     * @param event Event type
     * @return Flowable listener
     */
    private FlowableListener createFlowableListener(String taskListener, String event) {
        FlowableListener result = new FlowableListener();
        result.setEvent(event);
        result.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
        result.setImplementation("${" + taskListener + "}");
        return result;
    }
}
