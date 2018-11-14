package ru.citeck.ecos.menu.resolvers;

import ru.citeck.ecos.menu.dto.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserSitesResolver implements MenuItemsResolver {

    private static final String ID = "USER_SITES";

    @Override
    public List<Item> resolve(Map<String, String> params) {
        String userName = params.get(USER_NAME_PARAM_KEY);
        List<Item> result = new ArrayList<>();
        return result;
    }

    @Override
    public String getId() {
        return ID;
    }
}
