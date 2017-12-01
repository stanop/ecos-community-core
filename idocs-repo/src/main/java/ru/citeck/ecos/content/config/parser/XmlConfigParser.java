package ru.citeck.ecos.content.config.parser;

import ru.citeck.ecos.utils.XMLUtils;

import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class XmlConfigParser<T> implements ConfigParser<T> {

    private String rootClassName;
    private List<String> schemaFiles = Collections.emptyList();

    @Override
    public T parse(InputStream stream) {
        try {
            Class<?> rootClass = Class.forName(rootClassName);
            String[] schema = schemaFiles.toArray(new String[schemaFiles.size()]);
            Unmarshaller jaxbUnmarshaller = XMLUtils.createUnmarshaller(rootClass, schema);
            @SuppressWarnings("unchecked")
            T result = (T) jaxbUnmarshaller.unmarshal(stream);
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("Can not parse stream", e);
        }
    }

    public void setRootClassName(String rootClassName) {
        this.rootClassName = rootClassName;
    }

    public void setSchemaFiles(List<String> schemaFiles) {
        this.schemaFiles = schemaFiles;
    }
}
