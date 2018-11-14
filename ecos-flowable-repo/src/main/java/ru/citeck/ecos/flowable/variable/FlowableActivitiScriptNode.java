package ru.citeck.ecos.flowable.variable;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Scriptable;

import java.io.Serializable;
import java.util.Date;

public class FlowableActivitiScriptNode extends ScriptNode {

    private static final long serialVersionUID = 6650246004049727492L;

    public FlowableActivitiScriptNode(NodeRef nodeRef, ServiceRegistry services, Scriptable scope) {
        super(nodeRef, services, scope);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeValueConverter createValueConverter() {
        return new FlowableActivitiScriptNode.JBPMNodeConverter();
    }

    /**
     * Value converter for beanshell. Dates should be handled differently since
     * default conversion uses top-level scope which is sometimes missing.
     */
    private class JBPMNodeConverter extends NodeValueConverter {
        @Override
        public Serializable convertValueForRepo(Serializable value) {
            if (value instanceof Date) {
                return value;
            } else {
                return super.convertValueForRepo(value);
            }
        }

        @Override
        public Serializable convertValueForScript(ServiceRegistry serviceRegistry, Scriptable theScope, QName qname, Serializable value) {

            if (theScope == null) {
                theScope = scope;
            }

            if (value instanceof NodeRef) {
                return new FlowableActivitiScriptNode(((NodeRef) value), serviceRegistry, scope);
            } else if (value instanceof Date) {
                return value;
            } else {
                return super.convertValueForScript(serviceRegistry, theScope, qname, value);
            }
        }
    }
}
