package ru.citeck.ecos.service.namespace;

import java.util.Map;

public interface EcosNsPrefixProvider {

    Map<String, String> getPrefixesByNsURI();
}
