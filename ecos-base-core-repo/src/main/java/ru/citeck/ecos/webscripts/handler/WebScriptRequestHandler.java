package ru.citeck.ecos.webscripts.handler;

import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.Map;

public interface WebScriptRequestHandler {

    Map<String, Object> handleRequest(WebScriptRequest webScriptRequest);
}
