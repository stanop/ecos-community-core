package ru.citeck.ecos.content.converter;

import org.alfresco.service.namespace.QName;

import java.io.Serializable;

/**
 * Value converter to transmit properties between node and config data
 *
 * @author Pavel Simonov
 */
public interface ContentValueConverter {

    /**
     * Convert value from node property to config data
     * @param propName node property name
     */
    default String convertToConfigValue(QName propName, Serializable value) {
        return value != null ? value.toString() : null;
    }

    /**
     * Convert value from config data to node property
     * @param propName node property name
     */
    default Serializable convertToRepoValue(QName propName, String value) {
        return value;
    }

}

