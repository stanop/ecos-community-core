(function() {
	var nodeRefs = args.nodeRefs ? args.nodeRefs.split(",") : [];
	var fullNames = args.fullNames ? args.fullNames.split(",") : [];
	var authorities = [];
	
	for(var i in nodeRefs) {
		var node = search.findNode(nodeRefs[i]),
			authority = null;
		if(node == null) continue;
		if(node.isSubType("cm:person")) {
			authority = groups.getUser(node.properties["cm:userName"]);
		} else if(node.isSubType("cm:authorityContainer")) {
			authority = groups.getGroupForFullAuthorityName(node.properties["cm:authorityName"]);
		}
		if(authority == null) continue;
		authorities.push(authority);
	}
	
	for(var i in fullNames) {
		var fullName = fullNames[i],
			authority = null;
		if(fullName.match(/^GROUP_/)) {
			authority = groups.getGroupForFullAuthorityName(fullName);
		} else {
			authority = groups.getUser(fullName);
		}
		if(authority == null) continue;
		authorities.push(authority);
	}
	
	model.authorities = authorities;
})();