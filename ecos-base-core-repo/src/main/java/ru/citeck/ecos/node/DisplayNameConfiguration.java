package ru.citeck.ecos.node;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Map;

@Configuration
public class DisplayNameConfiguration {

    @Autowired
    private DisplayNameService displayNameService;

    @PostConstruct
    public void init() {
        displayNameService.register(ContentModel.TYPE_PERSON, this::evalPersonDisplayName);
        displayNameService.register(ContentModel.TYPE_CMOBJECT, this::evalDefaultDisplayName);
        displayNameService.register(ContentModel.TYPE_AUTHORITY_CONTAINER, this::evalAuthorityContainerDisplayName);
    }

    private String evalPersonDisplayName(AlfNodeInfo info) {

        Map<QName, Serializable> props = info.getProperties();

        String firstName = (String) props.get(ContentModel.PROP_FIRSTNAME);
        String lastName = (String) props.get(ContentModel.PROP_LASTNAME);

        StringBuilder result = new StringBuilder();
        if (StringUtils.isNotBlank(firstName)) {
            result.append(firstName);
        }
        if (StringUtils.isNotBlank(lastName)) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(lastName);
        }

        if (result.length() == 0) {
            String userName = (String) props.get(ContentModel.PROP_USERNAME);
            if (StringUtils.isNotBlank(userName)) {
                result.append(userName);
            } else {
                result.append(String.valueOf(info.getNodeRef()));
            }
        }

        return result.toString();
    }

    private String evalAuthorityContainerDisplayName(AlfNodeInfo info) {

        Map<QName, Serializable> props = info.getProperties();

        String displayName = (String) props.get(ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
        String authorityName = (String) props.get(ContentModel.PROP_AUTHORITY_NAME);

        return StringUtils.isNotBlank(displayName) ? displayName : authorityName;
    }

    public String evalDefaultDisplayName(AlfNodeInfo info) {

        Map<QName, Serializable> props = info.getProperties();

        String title = (String) props.get(ContentModel.PROP_TITLE);
        String name = (String) props.get(ContentModel.PROP_NAME);

        return StringUtils.isNotBlank(title) ? title : name;
    }
}
