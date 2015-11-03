<import resource="classpath:alfresco/templates/webscripts/ru/citeck/orgstruct/orgstruct.lib.js">
model.authorities = (function() {
	var groupName = url.templateArgs.groupname,
		group = groups.getGroup(groupName), 
		options = getFilterOptions();
		
	if(group == null) {
		status.setCode(status.STATUS_NOT_FOUND, "Group " + groupName + " not found");
		return;
	}

	return [].
		concat(filterAuthorities(args.recurse ? group.allGroups : group.childGroups, options)).
		concat(filterAuthorities(args.recurse ? group.allUsers  : group.childUsers , options));
})();