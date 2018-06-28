package ru.citeck.ecos.form.action;

import org.springframework.stereotype.Component;
import ru.citeck.ecos.form.action.handlers.FormActionHandler;

import java.util.*;

@Component
public class FormActionHandlerProvider {

    private Map<String, List<FormActionHandler>> handlers = new HashMap<>();
    private FormActionHandlersComparator comparator = new FormActionHandlersComparator();

    public List<FormActionHandler> getHandlersByTaskType(String taskType) {
        List<FormActionHandler> formActionHandlers = handlers.get(taskType);
        if (formActionHandlers == null) {
            formActionHandlers = Collections.emptyList();
        }
        return formActionHandlers;
    }

    public void subscribe(FormActionHandler handler) {
        String taskType = handler.getTaskType();
        List<FormActionHandler> handlersOfTaskType = handlers.get(taskType);
        if (handlersOfTaskType != null) {
            handlersOfTaskType.add(handler);
            handlersOfTaskType.sort(comparator);
        } else {
            ArrayList<FormActionHandler> newList = new ArrayList<>();
            newList.add(handler);
            handlers.put(taskType, newList);
        }
    }


    private static class FormActionHandlersComparator implements Comparator<FormActionHandler> {

        @Override
        public int compare(FormActionHandler o1, FormActionHandler o2) {
            return Integer.compare(o1.getOrder(), o2.getOrder());
        }

    }

}
