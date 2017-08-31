package ru.citeck.ecos.providers;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import java.util.Arrays;
import java.util.Collection;

/**
 * Application context provider
 */
public class ApplicationContextProvider implements ApplicationContextAware {
    /**
     * Application context
     */
    private static ApplicationContext context;

    /**
     * Get spring application context
     * @return Application context
     */
    public static ApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * Get spring bean
     * @param beanName Bean name
     * @param beanClass Bean class
     * @param <T> Type
     * @return Spring bean
     */
    public static <T> T getBean(String beanName, Class<T> beanClass) {
        return context.getBean(beanName, beanClass);
    }

    /**
     * Get spring bean
     * @param beanClass Bean class
     * @param <T> Type
     * @return Spring bean
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    /**
     * Get spring bean
     * @param beanName Bean name
     * @return Spring bean
     */
    public static Object getBean(String beanName) {
        return context.getBean(beanName);
    }

    /**
     * Get beans names
     * @param beansClass Beans class
     * @return Set of beans
     */
    public static Collection<String> getBeansNames(Class beansClass) {
        return Arrays.asList(context.getBeanNamesForType(beansClass));
    }

    /**
     * Get beans
     * @param beansClass Beans class
     * @param <T> Class type
     * @return Set of beans
     */
    public static <T> Collection<T> getBeans(Class<T> beansClass) {
        return context.getBeansOfType(beansClass).values();
    }

    /**
     * Set spring application context
     * @param applicationContext Application context
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}