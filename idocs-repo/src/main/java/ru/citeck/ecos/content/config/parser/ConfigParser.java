package ru.citeck.ecos.content.config.parser;

import java.io.InputStream;

public interface ConfigParser<T> {

    T parse(InputStream stream);

}
