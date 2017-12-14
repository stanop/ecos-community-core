package ru.citeck.ecos.behavior.base;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.*;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBehaviour {

    static final String DEFAULT_CLASS_NAME_FIELD = "className";
    static final String DEFAULT_ASSOC_NAME_FIELD = "assocName";

    private static final String QNAME_FIELD = "QNAME";

    private int order = OrderedBehaviour.DEFAULT_ORDER;

    private QName className = ContentModel.TYPE_CMOBJECT;
    private QName assocName = null;

    private List<JavaPolicyBehaviour> behaviours = new ArrayList<>();

    protected Log logger;

    protected ServiceRegistry serviceRegistry;
    protected DictionaryService dictionaryService;
    protected PolicyComponent policyComponent;
    protected NodeService nodeService;

    protected boolean enabled = true;
    private boolean initialized = false;

    public AbstractBehaviour() {
        logger = LogFactory.getLog(getClass());
    }

    @PostConstruct
    public void initialize() {
        synchronized (this) {
            if (enabled && !initialized) {
                beforeInit();
                registerPolicies();
                initialized = true;
            }
        }
    }

    /**
     * Setup before initialization
     */
    protected void beforeInit() {}

    private void registerPolicies() {

        Method[] methods = getClass().getMethods();

        for (Method method : methods) {

            PolicyMethod[] configs = method.getAnnotationsByType(PolicyMethod.class);

            for (PolicyMethod config : configs) {

                Class<? extends Policy> policy = config.policy();
                QName policyQName = getPolicyQName(policy);
                Behaviour behaviour = createBehaviour(method, config);

                QName className = getClassName(config);
                QName assocName = getAssocName(config);

                if (ClassPolicy.class.isAssignableFrom(policy)) {

                    policyComponent.bindClassBehaviour(policyQName, className, behaviour);

                } else if (AssociationPolicy.class.isAssignableFrom(policy)) {

                    if (assocName != null) {
                        policyComponent.bindAssociationBehaviour(policyQName, className, assocName, behaviour);
                    } else {
                        policyComponent.bindAssociationBehaviour(policyQName, className, behaviour);
                    }
                } else {
                    throw new IllegalStateException("Policy " + policy.getName() + " is not supported");
                }
            }
        }
    }

    private Behaviour createBehaviour(Method method, PolicyMethod config) {

        JavaPolicyBehaviour behaviour = new JavaPolicyBehaviour(this, method.getName(),
                                                                nodeService, config.frequency());
        behaviour.setRunAsSystem(config.runAsSystem());
        behaviour.setRecursive(config.recursive());
        behaviour.setFullEnabled(enabled);
        behaviour.setOrder(order);
        behaviours.add(behaviour);

        return behaviour;
    }

    private QName getPolicyQName(Class<?> policy) {
        return getFieldValue(policy, null, QNAME_FIELD, QName.class, false);
    }

    private QName getClassName(PolicyMethod config) {
        if (DEFAULT_CLASS_NAME_FIELD.equals(config.classField())) {
            return className;
        } else {
            return getFieldValue(null, this, config.classField(), QName.class, false);
        }
    }

    private QName getAssocName(PolicyMethod config) {
        if (DEFAULT_ASSOC_NAME_FIELD.equals(config.assocField())) {
            return assocName;
        } else {
            return getFieldValue(null, this, config.assocField(), QName.class, true);
        }
    }

    private <T> T getFieldValue(Class<?> clazz, Object obj, String fieldName, Class<T> type, boolean allowNull) {
        if (clazz == null) {
            clazz = obj.getClass();
        }
        try {
            Field field = getField(clazz, fieldName);
            field.setAccessible(true);
            Object result = field.get(obj);
            if (!allowNull && result == null) {
                throw new IllegalStateException("Field " + fieldName + " is null");
            }
            if (result != null && !type.isInstance(result)) {
                throw new IllegalStateException("Field " + fieldName + " has incorrect type: " +
                                                result.getClass().getName() + " expected: " +
                                                type.getName());
            }
            return result != null ? type.cast(result) : null;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Field not found in class " + clazz, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Illegal access to field in class " + clazz, e);
        }
    }

    private Field getField(Class clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw e;
            } else {
                return getField(superClass, fieldName);
            }
        }
    }

    public void setClassName(QName className) {
        ParameterCheck.mandatory("className", className);
        this.className = className;
    }

    public QName getClassName() {
        return className;
    }

    public void setAssocName(QName assocName) {
        this.assocName = assocName;
    }

    public QName getAssocName() {
        return assocName;
    }

    public void setOrder(int order) {
        this.order = order;
        for (JavaPolicyBehaviour behaviour : behaviours) {
            behaviour.setOrder(order);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            for (JavaPolicyBehaviour behaviour : behaviours) {
                behaviour.setFullEnabled(enabled);
            }
            if (enabled) initialize();
        }
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        dictionaryService = serviceRegistry.getDictionaryService();
        policyComponent = serviceRegistry.getPolicyComponent();
        nodeService = serviceRegistry.getNodeService();
    }
}
