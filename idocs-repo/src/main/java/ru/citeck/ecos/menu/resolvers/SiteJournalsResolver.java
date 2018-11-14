package ru.citeck.ecos.menu.resolvers;

import ru.citeck.ecos.menu.dto.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SiteJournalsResolver implements MenuItemsResolver {

    private static final String ID = "SITE_JOURNALS";

    @Override
    public List<Item> resolve(Map<String, String> params) {
        String context = params.get(CONTEXT_PARAM_KEY);
        List<Item> result = new ArrayList<>();
        return result;
    }

    @Override
    public String getId() {
        return ID;
    }

}
