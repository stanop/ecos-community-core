package ru.citeck.ecos.menu.resolvers;

import ru.citeck.ecos.menu.dto.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JournalsResolver implements MenuItemsResolver {

    private static final String id = "JOURNALS";

    @Override
    public List<Item> resolve(Map<String, String> params, String context) {
        List<Item> items = new ArrayList<>();
//        items.addAll();
        return items;
    }

    @Override
    public String getId() {
        return id;
    }
}
