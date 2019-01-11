package ru.citeck.ecos.records.meta;

import org.apache.commons.beanutils.PropertyUtils;
import ru.citeck.ecos.records.RecordMeta;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RecordsMetaServiceImpl implements RecordsMetaService {

    private Map<Class<?>, MetaClass> typesCache = new ConcurrentHashMap<>();

    @Override
    public Map<String, String> getAttributes(Class<?> metaClass) {

        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(metaClass);

        Map<String, String> attributes = new HashMap<>();

        for (PropertyDescriptor descriptor : descriptors) {



        }


        return null;
    }

    public List<AttributeSchema> createSchema(Map<String, String> attributes) {


    }

    @Override
    public <T> T createMeta(Class<?> metaClass, RecordMeta meta) {



        return null;
    }

    private static class MetaClass {

        Method idSetter;

        List<AttributeInfo> attributes;
    }

    private static class AttributeInfo extends MetaClass {

        String name;
        Method setter;
        String schema;
    }
}
