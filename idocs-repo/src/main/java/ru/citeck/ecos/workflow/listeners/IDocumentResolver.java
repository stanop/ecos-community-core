package ru.citeck.ecos.workflow.listeners;

import org.activiti.engine.delegate.VariableScope;
import org.alfresco.service.cmr.repository.NodeRef;

public interface IDocumentResolver {
    NodeRef getDocument(VariableScope execution);
    NodeRef getDocument(NodeRef wfPackage);
}
