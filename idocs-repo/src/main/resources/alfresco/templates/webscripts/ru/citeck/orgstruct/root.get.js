<import resource="classpath:alfresco/templates/webscripts/ru/citeck/orgstruct/orgstruct.lib.js">
model.authorities = (function() {
	var options = getFilterOptions(),
		paging = utils.createPaging(-1, 0);
		
	return [].
		concat(filterAuthorities(args.recurse ? groups.searchGroups(args.filter || "*") : groups.getAllRootGroups(), options)).
		concat(filterAuthorities(groups.searchUsers(args.filter || "*", paging, "userName"), options));
})();