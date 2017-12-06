package ru.citeck.ecos.content.config.dao;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ConfigDAO<T> {

    default T read(byte[] bytes) {
        return read(new ByteArrayInputStream(bytes));
    }

    T read(InputStream stream);

    default void write(T value, ContentWriter writer) {
        try (OutputStream stream = writer.getContentOutputStream()) {
            write(value, stream);
        } catch (IOException e) {
            throw new AlfrescoRuntimeException("Exception while write value to content writer", e);
        }
    }

    void write(T value, OutputStream stream);
}
