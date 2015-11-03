package ru.citeck.ecos.delegate;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.test.ApplicationContextHelper;

public class AvailabilityServiceImplTest {

	private PersonService personService;
	private AvailabilityService availabilityService;

	private static String TEST_USER1 = "testAvailabilityUser1";
	
	@Before
	public void beforeTest() {
		
		// mark transaction as rollback-only, so that repository state does not change
		// regardless of test execution status (success/failure)
		UserTransaction transaction = RetryingTransactionHelper.getActiveUserTransaction();
		if(transaction != null) {
			try {
				transaction.setRollbackOnly();
			} catch (SystemException e) {
				throw new IllegalStateException(e);
			}
		}
		
		ApplicationContext context = ApplicationContextHelper.getApplicationContext();
		ServiceRegistry serviceRegistry = context.getBean("ServiceRegistry", ServiceRegistry.class);
		
		personService = serviceRegistry.getPersonService();
		availabilityService = (AvailabilityService) serviceRegistry.getService(CiteckServices.AVAILABILITY_SERVICE);
		
		AuthenticationUtil.setRunAsUserSystem();
		
		Map<QName, Serializable> personProperties = new HashMap<QName, Serializable>(1);
		personProperties.put(ContentModel.PROP_USERNAME, TEST_USER1);
		personService.createPerson(personProperties);
		
	}
	
	@Test
	public void test() {

		// check availability manipulation
		
		assertTrue (availabilityService.getUserAvailability(TEST_USER1));
		availabilityService.setUserAvailability(TEST_USER1, true);
		assertTrue (availabilityService.getUserAvailability(TEST_USER1));
		availabilityService.setUserAvailability(TEST_USER1, false);
		assertFalse(availabilityService.getUserAvailability(TEST_USER1));
		availabilityService.setUserAvailability(TEST_USER1, true);
		assertTrue(availabilityService.getUserAvailability(TEST_USER1));


	}

	@After
	public void afterTest() {
		
		try { personService.deletePerson(TEST_USER1); } catch(Exception e) { /* ignore */ }
		
		AuthenticationUtil.clearCurrentSecurityContext();

	}
	
}
