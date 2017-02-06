package ru.citeck.ecos.test;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextLoader;

public class ApplicationContextHelper implements ApplicationContextAware, ContextLoader
{
	private static ApplicationContext context = null;

	public static ApplicationContext getApplicationContext() {
		if(context == null) {
			context = new ClassPathXmlApplicationContext(TestConstants.CONTEXT_LOCATIONS);
		}
		return context;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException 
	{
		context = applicationContext;
	}

    @Override
    public String[] processLocations(Class<?> cls, String... locations) {
        return TestConstants.CONTEXT_LOCATIONS;
    }

    @Override
    public ApplicationContext loadContext(String... locations) throws Exception {
        return getApplicationContext();
    }

}
