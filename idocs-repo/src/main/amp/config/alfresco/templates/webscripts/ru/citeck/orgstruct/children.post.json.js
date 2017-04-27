<import resource="classpath:alfresco/templates/webscripts/ru/citeck/orgmeta/orgmeta.lib.js">
<import resource="classpath:alfresco/templates/webscripts/ru/citeck/orgstruct/orgstruct.lib.js">
(function() {
	if(!url.templateArgs.groupname) {
		status.setCode(status.STATUS_BAD_REQUEST, "Parameter groupname should be specified");
		return;
	}
	var obj = eval('(' + json.toJSONString() + ')');
	model.authority = orgstruct.createAuthority(obj, url.templateArgs.groupname || null);
})();
