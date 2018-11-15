package ru.citeck.ecos.menu.resolvers;

import ru.citeck.ecos.menu.dto.Element;

import java.util.List;
import java.util.Map;

public interface MenuItemsResolver {

    List<Element> resolve(Map<String, String> params, Element context);

    String getId();

}
