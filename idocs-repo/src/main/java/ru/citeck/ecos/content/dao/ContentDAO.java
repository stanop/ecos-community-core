package ru.citeck.ecos.content.dao;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * DAO to marshal and unmarshal content
 *
 * @param <T> type of parsed content data
 *
 * @author Pavel Simonov
 */
public interface ContentDAO<T> {

    /**
     * Unmarshal config data
     */
    default T read(byte[] bytes) {
        return read(new ByteArrayInputStream(bytes));
    }

    /**
     * Unmarshal config data
     */
    T read(InputStream stream);

    /**
     * Marshal config data
     */
    default void write(T value, ContentWriter writer) {
        try (OutputStream stream = writer.getContentOutputStream()) {
            write(value, stream);
        } catch (IOException e) {
            throw new AlfrescoRuntimeException("Exception while write value to content writer", e);
        }
    }

    /**
     * Marshal config data
     */
    void write(T value, OutputStream stream);
}
