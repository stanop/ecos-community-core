package ru.citeck.ecos.flowable.temp;

import org.alfresco.repo.service.ServiceDescriptorRegistry;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.providers.ApplicationContextProvider;

/**
 * Created by impi on 13.10.17.
 */
public abstract class AbstractListener {
    protected ServiceRegistry serviceRegistry;

    protected Object getBean(String name) {
        return serviceRegistry.getService(QName.createQName(null, name));
    }

    @SuppressWarnings("unchecked")
    protected <T> T getBean(String name, Class<T> clazz) {
        Object bean = getBean(name);
        if(clazz.isInstance(bean)) {
            return (T) bean;
        } else {
            return null;
        }
    }

    protected <T> T getBean(Class<T> clazz) {
        return getBean(clazz.getSimpleName(), clazz);
    }

    protected final void init() {
        if(serviceRegistry == null) {
            serviceRegistry = ApplicationContextProvider.getBean(ServiceDescriptorRegistry.class);
            this.initImpl();
        }
    }

    protected void initImpl() {
        // subclasses can override this
    }
}
