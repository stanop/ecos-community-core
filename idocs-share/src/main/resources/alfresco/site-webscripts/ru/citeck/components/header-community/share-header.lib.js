function insertInHeaderMenu(item, index) {
  var headerMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_APP_MENU_BAR");

  if (headerMenu) {
    var indexOfLastElement = headerMenu.config.widgets.length - 1,
        lastElement = function() { return headerMenu.config.widgets[indexOfLastElement] };

    if (lastElement().config.label == "Debug Menu") indexOfLastElement--;
    if (lastElement().id && lastElement.id == "HEADER_MORE") indexOfLastElement--;

    headerMenu.config.widgets.splice(index || indexOfLastElement, 0, item);
  }
}

function insertInAdminGroup (item, index) {
  var adminMenuGroup = widgetUtils.findObject(model.jsonModel, "id", "HEADER_MORE_TOOLS_GROUP");

  if (adminMenuGroup) {
    var indexOfLastElement = adminMenuGroup.config.widgets.length - 1,
        lastElement = function() { return adminMenuGroup.config.widgets[indexOfLastElement] };

    if (lastElement().id && lastElement.id == "HEADER_MORE_TOOLS_MORE") indexOfLastElement--;

    adminMenuGroup.config.widgets.splice(index || indexOfLastElement, 0, item);
  }
}

function hasWidget(id) {
  return widgetUtils.findObject(model.jsonModel, "id", id) !== undefined && widgetUtils.findObject(model.jsonModel, "id", id) !== null;
}

function getWidget(id) {
  return widgetUtils.findObject(model.jsonModel, "id", id);
}

function buildItems(items, groupName) {
  var result = [];

  for (var i = 0; i < items.length; i++) {
    var id = "HEADER_" + (groupName + "_" + items[i].id.replace(/-/, "_")).toUpperCase(),
        configuration = {
          id: id,
          label: items[i].label || "header." + items[i].id + ".label",
          targetUrl: items[i].url,
          targetUrlType: items[i].urlType || "SHARE_PAGE_RELATIVE"
        };

    if (items[i].iconImage) configuration["iconImage"] = items[i].iconImage;
    if (items[i].movable) configuration["movable"] = items[i].movable;

    result.push({
      id: configuration.id,
      name: "alfresco/menus/AlfMenuItem",
      config: configuration
    });
  }

  return result;
};

function toMobileWidget(object) {
  var widget = jsonUtils.toObject(jsonUtils.toJSONObject(object));
  widget.id = widget.config.id = widget.id.replace("HEADER_", "HEADER_MOBILE_");

  if (widget.config.widgets && widget.config.widgets.length > 0) {
    for (var i in widget.config.widgets) {
      widget.config.widgets[i] = toMobileWidget(widget.config.widgets[i])
    }
  }
  
  return widget;
}

function buildSiteUrl(sitename) {
  return sitename ? "site/" + encodeURIComponent(sitename) + "/" : "";
};

function getHeaderMobileLogoUrl() {
    // Generate the source for the logo...
    var logoSrc = context.getSiteConfiguration().getProperty("mobileLogo");
    if (logoSrc && logoSrc.length() > 0) {
        // Use the site configured logo as the source for the logo image.
        logoSrc = url.context + "/proxy/alfresco/api/node/" + logoSrc.replace("://", "/") + "/content";
    } else {
        // Use the message bundled configured logo as the logo source.
        // This is theme specific
        var propsLogo = msg.get("header.mobile-logo");
        if (propsLogo == "header.mobile-logo") {
            propsLogo = "app-logo-mobile.png";
        }
        logoSrc = url.context + "/res/themes/" + theme + "/images/" + propsLogo;
    }
    return logoSrc;
}

function addSubWidgetsToSectionSlideMenu(sectionId, newItem, insetToTop) {
    var slideMenu = findObjectById(model.jsonModel.widgets, "HEADER_SLIDE_MENU");
    if (slideMenu && slideMenu.config && slideMenu.config.widgets && slideMenu.config.widgets.length) {
        slideMenu.config.widgets = slideMenu.config.widgets.map(function(widget) {
            if (widget.id == sectionId) {
                widget.widgets = widget.widgets || [];
                if (insetToTop == true) {
                    widget.widgets.unshift(newItem);
                } else {
                    widget.widgets.push(newItem);
                }
            }
            return widget;
        });
    }
}