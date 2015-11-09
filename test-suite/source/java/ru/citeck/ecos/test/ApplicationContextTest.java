package ru.citeck.ecos.test;


import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

public class ApplicationContextTest 
{
	private static ApplicationContext context = null;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		
		context = ApplicationContextHelper.getApplicationContext();

	}
	
	@Test
	public void simpleTest() {

		assertEquals(Boolean.TRUE, context != null);
		
	}

}
