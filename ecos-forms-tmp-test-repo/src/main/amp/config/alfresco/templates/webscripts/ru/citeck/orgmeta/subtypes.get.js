<import resource="classpath:alfresco/templates/webscripts/ru/citeck/orgmeta/orgmeta.lib.js">
(function() {
	var type = url.templateArgs.type;
	if(!checkGroupType(type)) {
		status.setCode(status.STATUS_NOT_FOUND, "Group type is not registered: " + type);
		return;
	}
	model.subTypes = orgmeta.getAllSubTypes(type);
})();