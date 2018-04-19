package ru.citeck.ecos.behavior.base;

import org.alfresco.repo.policy.BaseBehaviour;
import org.alfresco.repo.policy.PolicyException;
import org.alfresco.repo.policy.TransactionBehaviourOrder;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.surf.util.ParameterCheck;
import ru.citeck.ecos.utils.performance.MethodPerformance;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JavaPolicyBehaviour extends BaseBehaviour implements TransactionBehaviourOrder {

    // The object instance holding the method
    private Object instance;

    // The method name
    private String method;

    private int order = DEFAULT_ORDER;

    private boolean isFullEnabled = true;
    private boolean runAsSystem = false;
    private boolean recursive = false;

    private NodeService nodeService;

    /**
     * Construct.
     *
     * @param instance  the object instance holding the method
     * @param method  the method name
     */
    public JavaPolicyBehaviour(Object instance, String method, NodeService nodeService) {
        this(instance, method, nodeService, NotificationFrequency.EVERY_EVENT);
    }

    /**
     * Construct.
     *
     * @param instance  the object instance holding the method
     * @param method  the method name
     */
    public JavaPolicyBehaviour(Object instance, String method, NodeService nodeService, NotificationFrequency frequency) {
        super(frequency);
        ParameterCheck.mandatory("Instance", instance);
        ParameterCheck.mandatory("Method", method);
        this.method = method;
        this.instance = instance;
        this.nodeService = nodeService;
    }


    @Override
    public String toString() {
        return "Java method[class=" + instance.getClass().getName() + ", method=" + method + "]";
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T getInterface(Class<T> policy) {
        ParameterCheck.mandatory("Policy class", policy);
        Object proxy = proxies.get(policy);
        if (proxy == null)
        {
            InvocationHandler handler = getInvocationHandler(instance, method, policy);
            proxy = Proxy.newProxyInstance(policy.getClassLoader(), new Class[]{policy}, handler);
            proxies.put(policy, proxy);
        }
        return (T)proxy;
    }

    public Object getInstance() {
        return instance;
    }

    public String getMethod() {
        return method;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRunAsSystem(boolean runAsSystem) {
        this.runAsSystem = runAsSystem;
    }

    public boolean isRunAsSystem() {
        return runAsSystem;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

    boolean checkNodeRefs(Object[] args) {

        for (Object arg : args) {
            if (arg instanceof NodeRef) {
                if (!nodeRefExists((NodeRef) arg)) {
                    return false;
                }
            } else if (arg instanceof AssociationRef) {
                AssociationRef assocRef = (AssociationRef) arg;
                if (!nodeRefExists(assocRef.getSourceRef())
                        || !nodeRefExists(assocRef.getTargetRef())) {
                    return false;
                }
            } else if (arg instanceof ChildAssociationRef) {
                ChildAssociationRef assocRef = (ChildAssociationRef) arg;
                if (!nodeRefExists(assocRef.getParentRef())
                        || !nodeRefExists(assocRef.getChildRef())) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean nodeRefExists(NodeRef nodeRef) {
        return nodeRef != null && nodeService.exists(nodeRef);
    }

    public void setFullEnabled(boolean value) {
        isFullEnabled = value;
    }

    @Override
    public boolean isEnabled() {
        return isFullEnabled && super.isEnabled();
    }

    /**
     * Gets the Invocation Handler.
     *
     * @param <T>  the policy interface class
     * @param instance  the object instance
     * @param method  the method name
     * @param policyIF  the policy interface class
     * @return  the invocation handler
     */
    private <T> InvocationHandler getInvocationHandler(Object instance, String method, Class<T> policyIF) {

        Method[] policyIFMethods = policyIF.getMethods();

        if (policyIFMethods.length != 1) {
            throw new PolicyException("Policy interface " + policyIF.getCanonicalName() + " must have only one method");
        }

        try {
            Class instanceClass = instance.getClass();
            Method delegateMethod = instanceClass.getMethod(method, (Class[])policyIFMethods[0].getParameterTypes());
            return new JavaMethodInvocationHandler(this, delegateMethod);
        } catch (NoSuchMethodException e) {
            throw new PolicyException("Method " + method + " not found or accessible on " + instance.getClass(), e);
        }
    }

    /**
     * Java Method Invocation Handler
     *
     * @author David Caruana
     */
    private static class JavaMethodInvocationHandler implements InvocationHandler {

        private JavaPolicyBehaviour behaviour;
        private Method delegateMethod;

        /**
         * Constuct.
         *
         * @param behaviour  the java behaviour
         * @param delegateMethod  the method to invoke
         */
        private JavaMethodInvocationHandler(JavaPolicyBehaviour behaviour, Method delegateMethod) {
            this.behaviour = behaviour;
            this.delegateMethod = delegateMethod;
        }

        /* (non-Javadoc)
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            MethodPerformance perf = new MethodPerformance(behaviour.instance, delegateMethod, args);

            // Handle Object level methods
            switch (method.getName()) {
                case "toString":
                    return toString();
                case "hashCode":
                    return hashCode();
                case "equals":
                    return Proxy.isProxyClass(args[0].getClass())
                           && equals(Proxy.getInvocationHandler(args[0]));
            }

            // Delegate to designated method pointer
            if (behaviour.isEnabled()) {
                boolean disabled = false;
                try {
                    if (!behaviour.isRecursive()) {
                        behaviour.disable();
                        disabled = true;
                    }
                    if (behaviour.isRunAsSystem()) {
                        AuthenticationUtil.runAsSystem(() -> invokeImpl(args));
                    } else {
                        invokeImpl(args);
                    }
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                } finally {
                    if (disabled) {
                        behaviour.enable();
                    }
                    perf.stop();
                }
            }
            return null;
        }

        private Object invokeImpl(Object[] args) throws Exception {

            if (NotificationFrequency.TRANSACTION_COMMIT.equals(behaviour.getNotificationFrequency())) {
                if (behaviour.checkNodeRefs(args)) {
                    delegateMethod.invoke(behaviour.getInstance(), args);
                }
            } else {
                delegateMethod.invoke(behaviour.getInstance(), args);
            }

            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj == null || !(obj instanceof JavaMethodInvocationHandler)) {
                return false;
            }
            JavaMethodInvocationHandler other = (JavaMethodInvocationHandler)obj;
            return behaviour.instance.equals(other.behaviour.instance) && delegateMethod.equals(other.delegateMethod);
        }

        @Override
        public int hashCode() {
            return 37 * behaviour.instance.hashCode() + delegateMethod.hashCode();
        }

        @Override
        public String toString() {
            return "JavaBehaviour[instance=" + behaviour.instance.hashCode() + ", method=" + delegateMethod.toString() + "]";
        }
    }

}