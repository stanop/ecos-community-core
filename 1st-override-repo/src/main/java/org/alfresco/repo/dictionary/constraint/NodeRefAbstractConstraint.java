package org.alfresco.repo.dictionary.constraint;

import java.util.Collection;

import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

public abstract class NodeRefAbstractConstraint extends AbstractConstraint{

	protected abstract void evaluateSingleValue(Object value,NodeRef nodeRef);

    protected void evaluateSingleValue(QName field, Object value, NodeRef nodeRef){
        evaluateSingleValue(value, nodeRef);
    }

	protected void evaluateSingleValue(Object value) {
		// do nothing
	}
    /**
     * @see #evaluateSingleValue(Object)
     * @see #evaluateCollection(Collection)
     */
    @SuppressWarnings("unchecked")
    public final void evaluate(QName field, Object value,NodeRef nodeRef)
    {
        if (value == null)
        {
            // null values are never evaluated
            return;
        }
        try
        {
            // ensure that we can handle collections
            if (DefaultTypeConverter.INSTANCE.isMultiValued(value))
            {
                Collection collection = DefaultTypeConverter.INSTANCE.getCollection(Object.class, value);
                evaluateCollection(collection);
            }
            else
            {
                evaluateSingleValue(field,value,nodeRef);
            }
        }
        catch (ConstraintException e)
        {
            // this can go
            throw e;
        }
        catch (Throwable e)
        {
            throw new DictionaryException(AbstractConstraint.ERR_EVALUATE_EXCEPTION, this, e.getMessage());
        }
    }
}
