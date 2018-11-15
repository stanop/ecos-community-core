package ru.citeck.ecos.menu.deployer;

import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import ru.citeck.ecos.content.metadata.MetadataExtractor;
import ru.citeck.ecos.menu.xml.MenuConfig;
import ru.citeck.ecos.model.MenuConfigModel;

import java.io.Serializable;
import java.util.*;

public class MenuMetadataExtractor implements MetadataExtractor<MenuConfig> {

    @Override
    public Map<QName, Serializable> getMetadata(MenuConfig menuConfig) {

        Map<QName, Serializable> metadata = new HashMap<>();

        metadata.put(MenuConfigModel.PROP_ID, menuConfig.getId());
        metadata.put(MenuConfigModel.PROP_TYPE, menuConfig.getType());

        String authorities = menuConfig.getAuthorities();
        if (StringUtils.isNotBlank(authorities)) {
            List<String> authoritiesList = Arrays.asList(authorities.split(","));
            metadata.put(MenuConfigModel.PROP_AUTHORITIES, new ArrayList<>(authoritiesList));
        } else {
            metadata.put(MenuConfigModel.PROP_AUTHORITIES, new ArrayList<>(0));
        }

        return metadata;
    }
    
}
