package ru.citeck.ecos.behavior.base;

import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.Policy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to register method as policy behavior. Works only if your class extend AbstractBehaviour
 * Method name doesn't matter but it parameters must match with method of policy interface
 *
 * @author Pavel Simonov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PolicyMethod {

    /**
     * Policy for method. Allowed values: AssociationPolicy, ClassPolicy and all of their descendants
     * e.g. NodeServicePolicies.OnCreateNode.class
     * method arguments and policy method arguments must be the same
     */
    Class<? extends Policy> policy();

    /**
     * Field with class name for behaviour. You can specify own fields with type QName
     * and setup behaviour to use your field by this parameter. Field value is mandatory.
     */
    String classField() default AbstractBehaviour.DEFAULT_CLASS_NAME_FIELD;

    /**
     * Field with assoc name for behaviour. You can specify own fields with type QName
     * and setup behaviour to use your field by this parameter.
     * Field value is not mandatory and will be ignored if you register ClassPolicy
     */
    String assocField() default AbstractBehaviour.DEFAULT_ASSOC_NAME_FIELD;

    /**
     * Notification frequency. If you setup TRANSACTION_COMMIT then all NodeRefs
     * in arguments will be checked for existence. If any not exists, then invocation is ignored
     * Checked types: NodeRef, AssociationRef, ChildAssociationRef
     */
    NotificationFrequency frequency() default NotificationFrequency.EVERY_EVENT;

    /**
     * Invoke method with system permissions
     */
    boolean runAsSystem() default false;

    /**
     * May behaviour be invoked more than once in chain of calls or not
     * e.g. YourBehaviour -> OtherBehaviour1 -> OtherBehaviour2 -> YourBehaviour
     * If this argument is false then last invocation of YourBehaviour in example above will be ignored
     */
    boolean recursive() default false;
}
