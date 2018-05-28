package ru.citeck.ecos.content.metadata;

import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public interface MetadataExtractor<T> {

    default Map<QName, Serializable> getMetadata(T object) {
        return Collections.emptyMap();
    }
}
