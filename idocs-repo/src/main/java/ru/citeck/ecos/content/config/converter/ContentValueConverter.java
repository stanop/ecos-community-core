package ru.citeck.ecos.content.config.converter;

import org.alfresco.service.namespace.QName;

import java.io.Serializable;

public interface ContentValueConverter {

    default String convertToConfigValue(QName propName, Serializable value) {
        return value != null ? value.toString() : null;
    }

    default Serializable convertToRepoValue(QName propName, String value) {
        return value;
    }

}

