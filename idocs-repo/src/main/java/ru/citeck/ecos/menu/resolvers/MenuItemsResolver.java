package ru.citeck.ecos.menu.resolvers;

import ru.citeck.ecos.menu.dto.Item;

import java.util.List;
import java.util.Map;

public interface MenuItemsResolver {

    String CONTEXT_PARAM_KEY = "context";
    String USER_NAME_PARAM_KEY = "userName";

    List<Item> resolve(Map<String, String> params);

    String getId();

}
