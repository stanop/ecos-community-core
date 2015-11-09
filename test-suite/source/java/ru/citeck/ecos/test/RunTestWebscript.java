package ru.citeck.ecos.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class RunTestWebscript extends DeclarativeWebScript 
{
	private static final String PARAM_CLASS = "class";
	private static final String KEY_RESULT = "result";

	@Override
	public Map<String,Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
	
		String className = req.getParameter(PARAM_CLASS);
		if(className == null) {
			throw new IllegalArgumentException("Argument " + PARAM_CLASS + " is mandatory");
		}
		
		Class<?> cls = null;
		try {
			cls = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Class " + className + " not found");
		}
		
		Result result = JUnitCore.runClasses(cls);
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(KEY_RESULT, result);
		
		return model;
	}
	
}
