package ru.citeck.ecos.menu.dto;

import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.menu.resolvers.MenuItemsResolver;
import ru.citeck.ecos.menu.xml.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResolvedMenuConfigFactory {

    private Map<String, MenuItemsResolver> resolvers;

    public ResolvedMenuConfig getResolvedMenuConfig(MenuConfig menuConfigContentData, String username) {
        ResolvedMenuConfig resolvedMenuConfig = new ResolvedMenuConfig();
        resolvedMenuConfig.setId(menuConfigContentData.getId());
        resolvedMenuConfig.setType(menuConfigContentData.getType());

        List<Item> items = constructItems(menuConfigContentData.getItems(),
                menuConfigContentData.getId(), username);
        resolvedMenuConfig.setItems(items);

        return resolvedMenuConfig;
    }

    private List<Item> constructItems(Items items, String parentId, String userName) {

        List<Item> result = new ArrayList<>();

        if (items == null) {
            return null;
        }

        items.getItemsChildren().forEach(obj -> {
            if (obj instanceof ru.citeck.ecos.menu.xml.Item) {
                result.add(constructItem((ru.citeck.ecos.menu.xml.Item) obj, userName));
            } else if (obj instanceof ItemsResolver) {
                List<Item> resolvedItems = resolve((ItemsResolver) obj, parentId, userName);
                if (resolvedItems != null) {
                    result.addAll(resolvedItems);
                }
            }
        });

        return result;
    }

    private List<Item> resolve(ItemsResolver child, String parentId, String userName) {
        List<Item> resolvedItems = new ArrayList<>();
        String resolverId = child.getId();
        if (StringUtils.isEmpty(resolverId)) {
            return resolvedItems;
        }
        MenuItemsResolver resolver = resolvers.get(resolverId);
        if (resolver != null) {
            Map<String, String> params = new HashMap<>();
            params.put(MenuItemsResolver.USER_NAME_PARAM_KEY, userName);
            params.put(MenuItemsResolver.CONTEXT_PARAM_KEY , parentId);
            for (Parameter param : child.getParam()) {
                params.put(param.getName(), param.getValue());
            }
            resolvedItems.addAll(resolver.resolve(params));
            resolvedItems.forEach(item -> updateItem(item, child.getItem(), userName));
        }
        return resolvedItems;
    }

    private Item constructItem(ru.citeck.ecos.menu.xml.Item xmlItem, String userName) {
        Item newItem = new Item();
        return updateItem(newItem, xmlItem, userName);
    }

    private Item updateItem(Item targetItem, ru.citeck.ecos.menu.xml.Item newData, String userName) {
        if (targetItem == null) {
            return null;
        }
        if (newData == null) {
            return targetItem;
        }

        String label = fetchLocalizedMessage(newData.getLabel());
        String id = newData.getId();
        String icon = newData.getIcon();
        Boolean mobileVisible = newData.isMobileVisible();


        Action xmlAction = newData.getAction();
        if (xmlAction != null) {
            Map<String, String> params = new HashMap<>();
            for (Parameter xmlParam : xmlAction.getParam()) {
                params.put(xmlParam.getName(), xmlParam.getValue());
            }
            targetItem.setAction(xmlAction.getType(), params);
        }

        if (!StringUtils.isEmpty(id)) {
            targetItem.setId(id);
        }
        if (!StringUtils.isEmpty(label)) {
            targetItem.setLabel(label);
        }
        if (!StringUtils.isEmpty(icon)) {
            targetItem.setIcon(icon);
        }
        if (mobileVisible != null) {
            targetItem.setMobileVisible(mobileVisible);
        }

        targetItem.setItems(constructItems(newData.getItems(), targetItem.getId(), userName));

        return targetItem;
    }

    private String fetchLocalizedMessage(String messageKey) {
        if (StringUtils.isEmpty(messageKey)) {
            return StringUtils.EMPTY;
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
