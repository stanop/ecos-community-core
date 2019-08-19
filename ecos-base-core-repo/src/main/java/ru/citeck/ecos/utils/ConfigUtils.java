package ru.citeck.ecos.utils;

import org.apache.commons.lang.StringUtils;

public class ConfigUtils {

    public static Boolean strToBool(String value, Boolean def) {
        return StringUtils.isNotBlank(value) ? !Boolean.FALSE.toString().equals(value) : def;
    }
}
