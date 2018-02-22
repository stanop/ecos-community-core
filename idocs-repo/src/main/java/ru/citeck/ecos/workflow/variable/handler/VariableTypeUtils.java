package ru.citeck.ecos.workflow.variable.handler;

import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class VariableTypeUtils {

    public <T> T deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream ois = createObjectInputStream(bais);
            @SuppressWarnings("unchecked")
            T result = (T) ois.readObject();
            return result;
        } finally {
            IoUtil.closeSilently(bais);
        }
    }

    private ObjectInputStream createObjectInputStream(InputStream is) throws IOException {
        return new ObjectInputStream(is) {
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                return ReflectUtil.loadClass(desc.getName());
            }
        };
    }
}
