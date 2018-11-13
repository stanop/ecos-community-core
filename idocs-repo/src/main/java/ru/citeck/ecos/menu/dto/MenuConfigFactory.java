package ru.citeck.ecos.menu.dto;

import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.menu.resolvers.MenuItemsResolver;
import ru.citeck.ecos.menu.xml.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuConfigFactory {

    private Map<String, MenuItemsResolver> resolvers;

    public ResolvedMenuConfig getResolvedMenuConfig(MenuConfig menuConfigContentData, String username) {
        ResolvedMenuConfig resolvedMenuConfig = new ResolvedMenuConfig();
        resolvedMenuConfig.setId(menuConfigContentData.getId());
        resolvedMenuConfig.setType(menuConfigContentData.getType());

        List<Item> items = constructItems(menuConfigContentData.getItems(), username);
        resolvedMenuConfig.setItems(items);

        return resolvedMenuConfig;
    }

    private List<Item> constructItems(Items items, String userName) {

        if (items == null) {
            return null;
        }
        List<Object> children = items.getItemsChildren();
        List<Item> result = new ArrayList<>(children.size());

        for (Object child : children) {
            if (child instanceof ru.citeck.ecos.menu.xml.Item) {
                result.add(constructItem((ru.citeck.ecos.menu.xml.Item) child, userName));
            }
            else if (child instanceof ItemsResolver) {
//                TODO: finish items resolving
                List<Item> resolvedItems = resolve((ItemsResolver) child);
                if (resolvedItems != null) {
                    result.addAll(resolvedItems);
                }
            }
        }
        return result;
    }

    private List<Item> resolve(ItemsResolver child) {
        String resolverId = child.getId();
        if (StringUtils.isEmpty(resolverId)) {
            return null;
        }
        MenuItemsResolver resolver = resolvers.get(resolverId);
        if (resolver != null) {
            Map<String, String> params = new HashMap<>();
            params.put("user", "user");
            for (Parameter param : child.getParam()) {
                params.put(param.getName(), param.getValue());
            }
            return resolver.resolve(params, "");
        }
        return null;
    }

    private Item constructItem(ru.citeck.ecos.menu.xml.Item child, String userName) {
        Item newItem = new Item();

        Action xmlAction = child.getAction();
        if (xmlAction != null) {
            Map<String, String> params = new HashMap<>();
            for (Parameter xmlParam : xmlAction.getParam()) {
                params.put(xmlParam.getName(), xmlParam.getValue());
            }
            newItem.setAction(xmlAction.getType(), params);
        }

        newItem.setLabel(fetchLocalizedMessage(child.getLabel()));
        newItem.setId(child.getId());
        newItem.setIcon(child.getIcon());
        newItem.setMobileVisible(child.isMobileVisible());
        newItem.setItems(constructItems(child.getItems(), userName));

        return newItem;
    }

    private String fetchLocalizedMessage(String messageKey) {
        if (StringUtils.isEmpty(messageKey)) {
            return "";
        }
        String result = I18NUtil.getMessage(messageKey);
        if (StringUtils.isEmpty(result)) {
            return messageKey;
        }
        return result;
    }

    public void setResolvers(List<MenuItemsResolver> resolvers) {
        Map<String, MenuItemsResolver> result = new HashMap<>();
        resolvers.forEach(resolver -> result.put(resolver.getId(), resolver));
        this.resolvers = result;
    }

}
