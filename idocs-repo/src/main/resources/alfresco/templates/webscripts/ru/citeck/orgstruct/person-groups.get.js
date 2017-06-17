<import resource="classpath:alfresco/templates/webscripts/ru/citeck/orgstruct/orgstruct.lib.js">
model.authorities = (function() {
	var userName = url.templateArgs.username;
    if(!userName && args.nodeRef) {
        var userNode = search.findNode(args.nodeRef);
        if(!userNode) {
            status.setCode(status.STATUS_NOT_FOUND, "User " + args.nodeRef + " does not exist");
            return [];
        }
        userName = userNode.properties['cm:userName'];
    }

	var user = groups.getGroupForFullAuthorityName(userName), 
		options = getFilterOptions();
		
	if(user == null) {
		status.setCode(status.STATUS_NOT_FOUND, "User " + userName + " not found");
		return;
	}

	return filterAuthorities(user.allParentGroups, options);
})();