package ru.citeck.ecos.flowable.variable;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Roman Makarskiy
 */
public class FlowableScriptNode extends ScriptNode {

    private static final long serialVersionUID = 6650246004049727492L;

    public FlowableScriptNode(NodeRef nodeRef, ServiceRegistry services) {
        super(nodeRef, services, null);
    }

    @Override
    protected NodeValueConverter createValueConverter() {
        return new FlowableScriptNode.JBPMNodeConverter();
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
        public Serializable convertValueForScript(ServiceRegistry serviceRegistry, Scriptable theScope, QName qname,
                                                  Serializable value) {
            ensureScopePresent();
            if (theScope == null) {
                theScope = scope;
            }

            if (value instanceof NodeRef) {
                return new FlowableScriptNode(((NodeRef) value), serviceRegistry);
            } else if (value instanceof Date) {
                return value;
            } else {
                return super.convertValueForScript(serviceRegistry, theScope, qname, value);
            }
        }

        private void ensureScopePresent() {
            if (scope == null) {
                // Create a scope for the value conversion. This scope will be an empty scope exposing basic Object
                // and Function, sufficient for value-conversion.
                // In case no context is active for the current thread, we can safely enter end exit one to get hold
                // of a scope
                Context ctx = Context.getCurrentContext();
                boolean closeContext = false;
                if (ctx == null) {
                    ctx = Context.enter();
                    closeContext = true;
                }

                scope = ctx.initStandardObjects();
                scope.setParentScope(null);

                if (closeContext) {
                    // Only an exit call should be done when context didn't exist before
                    Context.exit();
                }
            }
        }
    }
}
