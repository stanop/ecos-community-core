package ru.citeck.ecos.eureka;

import java.util.Properties;
import java.util.function.Supplier;

public abstract class AbstractEurekaConfig {

    private static final String CONFIG_PREFIX = "ecos.eureka.";

    private Properties globalProperties;

    AbstractEurekaConfig(Properties globalProperties) {
        this.globalProperties = globalProperties;
    }

    protected int getGlobalIntParam(String globalKey, Supplier<Integer> orElse) {
        String result = globalProperties.getProperty(globalKey);
        if (result == null) {
            return orElse.get();
        }
        return Integer.parseInt(result);
    }

    protected int getIntParam(String localKey, Supplier<Integer> orElse) {
        return getGlobalIntParam(CONFIG_PREFIX + localKey, orElse);
    }

    protected Boolean getBoolParam(String localKey, Supplier<Boolean> orElse) {
        String result = globalProperties.getProperty(CONFIG_PREFIX + localKey);
        if (result == null) {
            return orElse.get();
        }
        return Boolean.TRUE.toString().equals(result);
    }

    protected String getGlobalStrParam(String globalKey, Supplier<String> orElse) {
        String result = globalProperties.getProperty(globalKey);
        if (result == null) {
            result = orElse.get();
        }
        return result;
    }

    protected String getStrParam(String localKey, Supplier<String> orElse) {
        return getGlobalStrParam(CONFIG_PREFIX + localKey, orElse);
    }
}
