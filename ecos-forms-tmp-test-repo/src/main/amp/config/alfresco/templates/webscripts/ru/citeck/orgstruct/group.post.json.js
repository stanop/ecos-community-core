<import resource="classpath:alfresco/templates/webscripts/ru/citeck/orgmeta/orgmeta.lib.js">
<import resource="classpath:alfresco/templates/webscripts/ru/citeck/orgstruct/orgstruct.lib.js">
(function() {
	var obj = eval('(' + json + ')');
	model.authority = createAuthority(obj, null);
})();
