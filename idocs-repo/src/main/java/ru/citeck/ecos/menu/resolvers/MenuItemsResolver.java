package ru.citeck.ecos.menu.resolvers;

import ru.citeck.ecos.menu.dto.Item;
import ru.citeck.ecos.menu.xml.ItemsResolver;

import java.util.List;
import java.util.Map;

public interface MenuItemsResolver {

    List<Item> resolve(Map<String, String> params, String context);

    String getId();

}
