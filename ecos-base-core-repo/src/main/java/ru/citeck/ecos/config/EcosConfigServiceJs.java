package ru.citeck.ecos.config;

import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

/**
 * @author Roman Makarskiy
 */
public class EcosConfigServiceJs extends AlfrescoScopableProcessorExtension {

    private EcosConfigService ecosConfigService;


    public Object getParamValue(final String key) {
        return ecosConfigService.getParamValue(key);
    }

    /**
     * @deprecated
     */
    public Object getParamValue(final String key, String rootPath) {
        return ecosConfigService.getParamValue(key);
    }

    public void setValue(final String key, final String value) {
        ecosConfigService.setValue(key, value);
    }

    /**
     * @deprecated
     */
    public void setValue(final String key, final String value, String rootPath) {
        ecosConfigService.setValue(key, value);
    }


    public void setEcosConfigService(EcosConfigService ecosConfigService) {
        this.ecosConfigService = ecosConfigService;
    }

}