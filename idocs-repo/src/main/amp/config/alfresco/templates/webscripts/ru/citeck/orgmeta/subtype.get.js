<import resource="classpath:alfresco/templates/webscripts/ru/citeck/orgmeta/orgmeta.lib.js">
(function() {
	var type = url.templateArgs.type,
		name = url.templateArgs.name;
	if(!checkGroupType(type)) {
		status.setCode(status.STATUS_NOT_FOUND, "Group type is not registered: " + type);
		return;
	}
	model.subType = orgmeta.getSubType(type, name);
	if(!model.subType) {
		status.setCode(status.STATUS_NOT_FOUND, "Group sub-type is not found: " + type + " - " + name);
		return;
	}
})();