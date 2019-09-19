package ru.citeck.ecos.utils;

import org.alfresco.service.cmr.repository.MLText;
import org.springframework.extensions.surf.util.I18NUtil;

import java.util.Locale;

public class EcosU18NUtils {

    public static final Locale RUSSIAN = new Locale("ru");

    public static final Locale[] LOCALES = {
            RUSSIAN,
            Locale.ENGLISH
    };

    public static MLText getMLText(String key, Object... args) {

        MLText mlText = getMLText(key);

        if (args == null || args.length == 0) {
            return mlText;
        }

        MLText result = new MLText();
        mlText.forEach((k, v) -> result.put(k, String.format(v, args)));
        return result;
    }

    public static MLText getMLText(String key) {

        MLText result = new MLText();

        for (Locale locale : LOCALES) {
            String msg = I18NUtil.getMessage(key, Locale.ENGLISH);
            if (msg != null) {
                result.put(locale, msg);
            }
        }
        if (result.isEmpty()) {
            result.put(I18NUtil.getLocale(), key != null ? key : "");
        }

        return result;
    }
}
