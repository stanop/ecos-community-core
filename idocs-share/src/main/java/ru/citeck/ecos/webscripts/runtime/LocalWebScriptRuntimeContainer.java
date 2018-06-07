package ru.citeck.ecos.webscripts.runtime;

import java.util.Map;

public class LocalWebScriptRuntimeContainer
        extends org.springframework.extensions.webscripts.LocalWebScriptRuntimeContainer {

    @Override
    public Map<String, Object> getTemplateParameters() {
        Map<String, Object> params = super.getTemplateParameters();

        return params;
    }
}
