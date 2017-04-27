<import resource="classpath:alfresco/templates/webscripts/ru/citeck/orgstruct/orgstruct.lib.js">
(function() {
    var options = getFilterOptions(),
        paging = utils.createPaging(-1,0);
	var allUsers = options.user ? groups.searchUsers("*", paging, null) : [];
    var allGroups = options.group || options.branch || options.role ? groups.getGroups(null, paging) : [];
	model.authorities = filterAuthorities(allGroups.concat(allUsers), options);
})();