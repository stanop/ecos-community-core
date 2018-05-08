package ru.citeck.ecos.config;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

/**
 * @author Roman Makarskiy
 */
public class EcosConfigServiceJs extends AlfrescoScopableProcessorExtension {

    @Autowired
    private EcosConfigService ecosConfigService;

    public Object getParamValue(final String key) {
        return ecosConfigService.getParamValue(key);
    }

    public Object getParamValue(final String key, String rootPath) {
        return ecosConfigService.getParamValue(key, rootPath);
    }

    public void setValue(final String key, final String value) {
        ecosConfigService.setValue(key, value);
    }

    public void setValue(final String key, final String value, String rootPath) {
        ecosConfigService.setValue(key, value, rootPath);
    }
}