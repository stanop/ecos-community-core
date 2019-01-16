package ru.citeck.ecos.content.dao;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JsonContentDAO<T> implements ContentDAO<T> {

    private ObjectMapper objectMapper = new ObjectMapper();
    private Class<T> contentType;

    @Override
    public T read(InputStream stream) throws IOException {
        return objectMapper.readValue(stream, contentType);
    }

    @Override
    public void write(T value, OutputStream stream) throws IOException {
        objectMapper.writeValue(stream, value);
    }

    public void setContentType(Class<T> contentType) {
        this.contentType = contentType;
    }
}
