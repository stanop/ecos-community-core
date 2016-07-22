if (user.isAdmin) {
  var headerMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_APP_MENU_BAR"),
      metajournals = widgetUtils.findObject(model.jsonModel, "id", "HEADER_MORE_TOOLS_META_JOURNALS");

  if (headerMenu && !metajournals) {
    headerMenu.config.widgets.push({
      id: "HEADER_MORE_TOOLS_META_JOURNALS",
      name: "alfresco/menus/AlfMenuBarItem",
      config: {
        label: "header.meta_journals.label",
        targetUrl: "journals2/list/meta"
      }
    });
  }
}
