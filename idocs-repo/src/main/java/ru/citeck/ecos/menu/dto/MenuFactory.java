package ru.citeck.ecos.menu.dto;

import org.alfresco.service.cmr.action.ActionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.menu.resolvers.MenuEvaluator;
import ru.citeck.ecos.menu.resolvers.MenuItemsResolver;
import ru.citeck.ecos.menu.xml.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MenuFactory {

    private final Map<String, MenuItemsResolver> resolvers = new HashMap<>();
    private ActionService actionService;

    public Menu getResolvedMenu(MenuConfig menuConfigContentData) {
        Menu menu = new Menu();
        menu.setId(menuConfigContentData.getId());
        menu.setType(menuConfigContentData.getType());

        List<Element> elements = constructItems(menuConfigContentData.getItems(), null);
        menu.setItems(elements);
        return menu;
    }

    private List<Element> constructItems(Items items, Element context) {
        if (items == null) {
            return Collections.emptyList();
        }
        List<Element> result = new ArrayList<>();
        items.getItemsChildren()
                .forEach(obj -> {
                    if (obj instanceof Item && evaluate((Item) obj)) {
                        Element newElement = new Element();
                        if (context == null) {
                            Map<String, String> params = new HashMap<>();
                            params.put("rootElement", "true");
                            newElement.setParams(params);
                        }
                        result.add(updateItem(newElement, (Item) obj));
                    } else if (obj instanceof ItemsResolver) {
                        result.addAll(resolve((ItemsResolver) obj, context));
                    }
                });
        return filterElements(result, context);
    }

    private List<Element> filterElements(List<Element> elements, Element context) {
        if (context == null || context.getParams() == null) {
            return elements;
        }
        String hideParam = StringUtils.defaultString(context.getParams().get("hideEmpty"));
        if (!hideParam.equals("true")) {
            return elements;
        }
        Predicate<Element> predicate = elem -> {
            boolean ignore = false;
            if (elem.getParams() != null) {
                String ignoreParam = StringUtils.defaultString(elem.getParams().get("ignoreHideEmpty"));
                ignore = ignoreParam.equals("true");
            }
            if (ignore) {
                return true;
            }
            return CollectionUtils.isNotEmpty(elem.getItems());
        };
        return elements.stream().filter(predicate).collect(Collectors.toList());
    }

    private boolean evaluate(Item item) {
        Evaluator evaluator = item.getEvaluator();
        if (evaluator == null) {
            return true;
        }
        MenuEvaluator eval = new MenuEvaluator(item.getEvaluator(), actionService);
        return eval.evaluate();
    }

    private List<Element> resolve(ItemsResolver child, Element context) {
        String resolverId = child.getId();
        if (StringUtils.isEmpty(resolverId)) {
            return Collections.emptyList();
        }
        MenuItemsResolver resolver = resolvers.get(resolverId);
        if (resolver == null) {
            return Collections.emptyList();
        }
            Map<String, String> params = new HashMap<>();
            for (Parameter param : child.getParam()) {
                params.put(param.getName(), param.getValue());
            }

            return resolver.resolve(params, context).stream()
                    .map(element -> updateItem(element, child.getItem()))
                    .collect(Collectors.toList());
    }

    private Element updateItem(Element targetElement, Item newData) {
        if (targetElement == null) {
            return null;
        }
        if (newData == null) {
            return targetElement;
        }

        String label = getLocalizedMessage(newData.getLabel());
        String id = newData.getId();
        String icon = newData.getIcon();
        List<Parameter> param = newData.getParam();

        Boolean mobileVisible = newData.isMobileVisible();

        Action xmlAction = newData.getAction();
        if (xmlAction != null) {
            Map<String, String> params = new HashMap<>();
            for (Parameter xmlParam : xmlAction.getParam()) {
                params.put(xmlParam.getName(), xmlParam.getValue());
            }
            targetElement.setAction(xmlAction.getType(), params);
        }

        if (!StringUtils.isEmpty(id)) {
            targetElement.setId(id);
        }
        if (!StringUtils.isEmpty(label)) {
            targetElement.setLabel(label);
        }
        if (!StringUtils.isEmpty(icon)) {
            targetElement.setIcon(icon);
        }
        if (mobileVisible != null) {
            targetElement.setMobileVisible(mobileVisible);
        }

        if (param != null) {
            Map<String, String> params = targetElement.getParams();
            if (params == null) {
                params = new HashMap<>();
            }
            Map<String, String> newParams = new HashMap<>();
            param.forEach(parameter -> newParams.put(parameter.getName(), parameter.getValue()));
            params.putAll(newParams);
            targetElement.setParams(params);
        }

        targetElement.setItems(constructItems(newData.getItems(), targetElement));

        return targetElement;
    }

    private String getLocalizedMessage(String messageKey) {
        if (StringUtils.isEmpty(messageKey)) {
            return StringUtils.EMPTY;
        }
        String result = I18NUtil.getMessage(messageKey);
        if (StringUtils.isEmpty(result)) {
            return messageKey;
        }
        return result;
    }

    public void addResolver(MenuItemsResolver menuItemsResolver) {
        this.resolvers.put(menuItemsResolver.getId(), menuItemsResolver);
    }

    @Autowired
    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }
}
