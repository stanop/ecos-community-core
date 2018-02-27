<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/citeck.lib.js">

var isSlideMenu = getMenuConfig("default-ui-main-menu") == "left";

if (user.isAdmin && !isSlideMenu) {
    var headerMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_APP_MENU_BAR"),
        metajournals = widgetUtils.findObject(model.jsonModel, "id", "HEADER_MORE_TOOLS_META_JOURNALS");

    if (headerMenu && !metajournals) {
        var indexOfLastElement = headerMenu.config.widgets.length - 1,
            lastElement = function() { return headerMenu.config.widgets[indexOfLastElement] };

        if (lastElement().config.label == "Debug Menu") indexOfLastElement--;
        if (lastElement().id && lastElement.id == "HEADER_MORE") indexOfLastElement--;

        headerMenu.config.widgets.splice(indexOfLastElement, 0, {
            id: "HEADER_META_JOURNALS",
            name: "alfresco/menus/AlfMenuBarItem",
            config: {
                label: "header.meta_journals.label",
                targetUrl: "journals2/list/meta"
            }
        });
    }
}
