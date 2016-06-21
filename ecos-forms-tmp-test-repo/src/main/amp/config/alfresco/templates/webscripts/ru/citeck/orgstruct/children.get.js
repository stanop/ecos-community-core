<import resource="classpath:alfresco/templates/webscripts/ru/citeck/orgstruct/orgstruct.lib.js">
model.authorities = (function() {
	var groupName = url.templateArgs.groupname,
		group = groups.getGroup(groupName), 
		options = getFilterOptions(),
		needGroups = options.group || options.branch || options.role,
		needUsers = options.user,
		childGroups = [],
		childUsers = [];
		
	if(group == null) {
		status.setCode(status.STATUS_NOT_FOUND, "Group " + groupName + " not found");
		return;
	}

    if(args.recurse != "true") {
        childGroups = needGroups ? group.childGroups : [];
        childUsers = needUsers ? group.childUsers : [];
    } else if(args.filter) {
        var filter = "*" + args.filter + "*";
        var matchingGroups = needGroups ? groups.searchGroups(filter) : [];
        var matchingUsers = needUsers ? groups.searchUsers(filter, utils.createPaging(-1, 0), "userName") : [];
        
        var groupContains = function(authority) {
            var parentGroups = authority.parentGroups;
            var nameToSearch = group.shortName;
            for(var i in parentGroups) {
                if(parentGroups[i].shortName == groupName) {
                    return true;
                }
            }
            for(var i in parentGroups) {
                if(cachedGroupContains(parentGroups[i])) {
                    return true;
                }
            }
            return false;
        };
        var containsCache = {};
        var cachedGroupContains = function(authority) {
            var authorityName = authority.fullName;
            if(typeof containsCache[authorityName] == "boolean") 
                return containsCache[authorityName];
            
            return containsCache[authorityName] = groupContains(authority);
        };
        
        for(var i in matchingGroups) {
            if(cachedGroupContains(matchingGroups[i])) {
                childGroups.push(matchingGroups[i]);
            }
        }
        
        for(var i in matchingUsers) {
            // no need to use cache for users
            if(groupContains(groups.getGroupForFullAuthorityName(matchingUsers[i].userName))) {
                childUsers.push(matchingUsers[i]);
            }
        }
        
    } else {
        childGroups = needGroups ? group.allGroups : [];
        childUsers = needUsers ? group.allUsers : [];
    }
    
    return filterAuthorities(childGroups.concat(childUsers), options);
})();