package ru.citeck.ecos.config.patch;

import lombok.Setter;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.config.EcosConfigService;
import ru.citeck.ecos.model.ConfigModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CreateConfig extends AbstractModuleComponent {

    private EcosConfigService ecosConfigService;

    private String configKey;
    private String configValue;
    private String configTitle;
    private String configDescription;

    @Autowired
    private NodeService nodeService;

    @Setter
    private Boolean isUpdateValueIfConfExists;

    @Override
    protected void executeInternal() {

        Map<QName, Serializable> props = new HashMap<>();
        props.put(ConfigModel.PROP_KEY, configKey);
        props.put(ConfigModel.PROP_VALUE, configValue);
        props.put(ContentModel.PROP_TITLE, getMLText(configTitle));
        props.put(ContentModel.PROP_DESCRIPTION, getMLText(configDescription));

        if (BooleanUtils.isTrue(isUpdateValueIfConfExists)) {
            Optional<NodeRef> configRef = ecosConfigService.getConfigRef(configKey);
            if (configRef.isPresent() && nodeService.exists(configRef.get())) {
                ecosConfigService.setValue(configKey, configValue);
                return;
            }
        }

        ecosConfigService.createConfig(props);
    }

    private Serializable getMLText(String key) {

        if (key == null) {
            return null;
        }

        String defaultMsg = I18NUtil.getMessage(key);

        if (StringUtils.isBlank(defaultMsg)) {
            return key;
        }

        MLText result = new MLText();
        addText(result, key, Locale.ENGLISH);
        addText(result, key, new Locale("ru"));

        if (result.isEmpty()) {
            result.put(Locale.ENGLISH, defaultMsg);
        }

        return result;
    }

    private void addText(MLText mlText, String key, Locale locale) {
        String msg = I18NUtil.getMessage(key, locale);
        if (msg != null) {
            mlText.put(locale, msg);
        }
    }

    public void setConfigTitle(String configTitle) {
        this.configTitle = configTitle;
    }

    public void setConfigDescription(String configDescription) {
        this.configDescription = configDescription;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    @Autowired
    @Qualifier("ecosConfigService")
    public void setEcosConfigService(EcosConfigService ecosConfigService) {
        this.ecosConfigService = ecosConfigService;
    }
}
