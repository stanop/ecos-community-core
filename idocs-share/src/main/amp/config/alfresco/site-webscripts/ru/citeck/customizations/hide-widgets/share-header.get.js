if (!user.isAdmin) {
    var titleMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_TITLE_MENU");

    if (titleMenu && titleMenu.config.widgets.length) {
        titleMenu.config.widgets = titleMenu.config.widgets.filter(function(item) {
            return item.id !== "HEADER_SITE_CONFIGURATION_DROPDOWN";
        })
    }
}