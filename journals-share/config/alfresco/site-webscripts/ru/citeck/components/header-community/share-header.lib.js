function insertInHeaderMenu(item) {
  var headerMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_APP_MENU_BAR");

  if (headerMenu) {
    var indexOfLastElement = headerMenu.config.widgets.length - 1,
        lastElement = function() { return headerMenu.config.widgets[indexOfLastElement] };

    if (lastElement().config.label == "Debug Menu") indexOfLastElement--;
    if (lastElement().id && lastElement.id == "HEADER_MORE") indexOfLastElement--;

    headerMenu.config.widgets.splice(indexOfLastElement, 0, item);
  }
}

function insertInAdminGroup (item) {
  var adminMenuGroup = widgetUtils.findObject(model.jsonModel, "id", "HEADER_MORE_TOOLS_GROUP");

  if (adminMenuGroup) {
    var indexOfLastElement = adminMenuGroup.config.widgets.length - 1,
        lastElement = function() { return adminMenuGroup.config.widgets[indexOfLastElement] };

    if (lastElement().id && lastElement.id == "HEADER_MORE_TOOLS_MORE") indexOfLastElement--;

    adminMenuGroup.config.widgets.splice(indexOfLastElement, 0, item);
  }
}

function hasWidget(id) {
  return widgetUtils.findObject(model.jsonModel, "id", id) !== undefined && widgetUtils.findObject(model.jsonModel, "id", id) !== null;
}
