package ru.citeck.ecos.graphql.meta.converter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.alfresco.util.Pair;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ConvertersProvider {

    private LoadingCache<Class<? extends MetaConverter<?>>, Optional<MetaConverter<?>>> customConverters;
    private LoadingCache<Class<?>, PojoConverter<?>> pojoConverters;

    private Map<Class<?>, MetaConverter<?>> defaultConverters = new ConcurrentHashMap<>();

    public ConvertersProvider() {

        this.customConverters = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build(CacheLoader.from(this::createCustomConverter));

        this.pojoConverters = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build(CacheLoader.from(this::createPojoConverter));

        register(DateConverter.class);
        register(StringConverter.class);
        register(NodeRefConverter.class);
    }

    public <T> MetaConverter<T> getConverter(Class<T> clazz) {

        MetaConverter<?> converter = defaultConverters.get(clazz);
        if (converter == null) {
            converter = pojoConverters.getUnchecked(clazz);
        }

        @SuppressWarnings("unchecked")
        MetaConverter<T> typedConv = (MetaConverter<T>) converter;
        return typedConv;
    }

    public <T> MetaConverter<T> getCustom(Class<? extends MetaConverter<T>> converterType) {
        MetaConverter<?> converter = customConverters.getUnchecked(converterType).orElse(null);
        @SuppressWarnings("unchecked")
        MetaConverter<T> typedConv = (MetaConverter<T>) converter;
        return typedConv;
    }

    private PojoConverter<?> createPojoConverter(Class<?> clazz) {
        return new PojoConverter<>(clazz, this);
    }

    private Optional<MetaConverter<?>> createCustomConverter(Class<? extends MetaConverter<?>> converterType) {
        try {
            return Optional.of(converterType.newInstance());
        } catch (ReflectiveOperationException e) {
            return Optional.empty();
        }
    }

    private Pair<Class, ? extends MetaConverter> createConverter(Class<? extends MetaConverter<?>> converter) {
        ParameterizedType type = (ParameterizedType) converter.getGenericSuperclass();
        Class classToConvert = (Class) type.getActualTypeArguments()[0];
        try {
            return new Pair<>(classToConvert, converter.newInstance());
        } catch (Exception e) {
            throw new RuntimeException("Class " + converter + " can't be instantiated", e);
        }
    }

    public void register(Collection<? extends Class<MetaConverter<?>>> converters) {
        converters.forEach(this::register);
    }

    public void register(Class<? extends MetaConverter<?>> converter) {
        Pair<Class, ? extends MetaConverter> data = createConverter(converter);
        defaultConverters.put(data.getFirst(), data.getSecond());
    }
}
