package ru.citeck.ecos.webscripts.handler;

import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.Map;

/**
 * Handler for handling request params and input data.
 * <p>
 * It's additional layer, which purpose relieve some responsibility from the web script class.
 * </p>
 */
public interface WebScriptRequestHandler {

    Map<String, Object> handleRequest(WebScriptRequest webScriptRequest);
}
