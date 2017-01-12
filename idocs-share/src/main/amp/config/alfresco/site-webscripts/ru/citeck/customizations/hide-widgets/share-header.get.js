if (!user.isAdmin) {
 	widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_SITE_CONFIGURATION_DROPDOWN");
}