package ru.citeck.ecos.form.action.handlers;

import org.alfresco.service.cmr.repository.NodeRef;

public interface FormActionHandler {

    void handle(NodeRef caseRef, String outcome);

    int getOrder();

    String getTaskType();

}
