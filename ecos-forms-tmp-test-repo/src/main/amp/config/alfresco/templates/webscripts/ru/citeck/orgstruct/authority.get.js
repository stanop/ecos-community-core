<import resource="classpath:alfresco/templates/webscripts/ru/citeck/orgstruct/orgstruct.lib.js">
(function() {
	var fullname = url.templateArgs.fullname;
	var authority = fullname.match(/^GROUP_/) ? groups.getGroupForFullAuthorityName(fullname) : groups.getUser(fullname);
	if(authority == null) {
		status.setCode(status.STATUS_NOT_FOUND, "Authority " + fullname + " was not found");
		return;
	}
	model.authority = authority;
})();