if (user.isAdmin) {
     var headerMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_APP_MENU_BAR");
    if (headerMenu != null) {
        headerMenu.config.widgets.push({
            id: "HEADER_CUSTOM_PROFILE_LINK",
            name: "alfresco/menus/AlfMenuBarItem",
            config: {
                label: msg.get("header.meta_journals.label"),
                targetUrl: "journals2/list/meta"
            }
        });
    }
}