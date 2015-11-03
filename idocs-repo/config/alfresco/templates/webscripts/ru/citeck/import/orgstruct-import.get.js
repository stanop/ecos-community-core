function readConfig() {
	var cfg = new XML(config.script);
	var recordName = cfg.recordName;
	var groupTypes = {};
	for(var i in cfg.groupTypes..groupType) {
		var groupType = cfg.groupTypes.groupType[i];
		var groupTypeCfg = {
			id: groupType.@id + "",
			shortName: groupType.@shortName + "",
			displayName: groupType.@displayName + "",
			xmlField: groupType.@xmlField + "",
			groupType: groupType.@groupType + "",
			groupSubtype: groupType.@groupSubtype + "",
			scope: (groupType.@scope + "").split(","),
			merge: (groupType.@merge == "true") ? true : false,
			parents: groupType.@parents + "" ? (groupType.@parents + "").split(",") : [],
		};
		groupTypes[groupType.@id] = groupTypeCfg;
	}
	var userTypes = {};
	for(var i in cfg.userTypes..userType) {
		var userType = cfg.userTypes.userType[i];
		var userTypeCfg = {
			id: userType.@id + "",
			xmlField: userType.@xmlField + "",
			alfField: userType.@alfField + "",
			scope: (userType.@scope + "").split(","),
			caseInsensitive: (userType.@caseInsensitive == "true") ? true : false
		};
		userTypes[userType.@id] = userTypeCfg;
	}
	return {
		recordName: recordName,
		groupTypes: groupTypes,
		userTypes: userTypes,
	};
}

function getAllGroupsByDisplayName() {
	// return all results, skip 0
	var paging = utils.createPaging(-1, 0);
	var grps = groups.getGroups(null, paging);

	var result = {};
	for(var i in grps) {
		var group = grps[i];
		var key = group.displayName;
		if(typeof result[key] == "undefined") {
			result[key] = [];
		}
		result[key].push(group);
	}
	return result;
}

function getAllUsersById(userType) {
	var users = {},
	    propName = userType.alfField,
	    caseInsensitive = userType.caseInsensitive;
	var p = groups.searchUsers('*', utils.createPaging(-1,0), "userName");
	for (var i = 0; i < p.length; i++) {
		var item = p[i].person;
		if (item) {
			var id = item.properties[propName];
			if(id) {
			    if(caseInsensitive) {
			        id = id.toLowerCase();
			    }
				users[id] = item;
			}
		}
	}
	return users;
}

function substitute(template, vars) {
	var result = template;
	while(result.match(/[{][^{}]+[}]/)) {
		var name = result.replace(/^.*[{]([^{}]+)[}].*$/, '$1');
		var value = vars[name];
		result = result.replace(new RegExp('[{]' + name + '[}]'), typeof value != "undefined" ? value : "");
	}
	return result;
}

function merge(obj1, obj2) {
	var obj3 = {};
	for(var i in obj1) obj3[i] = obj1[i];
	for(var i in obj2) obj3[i] = obj2[i];
	return obj3;
}

var globalScope = {
	shortName: "global",
	displayName: "global",
	type: "global",
	children: {}
};
var indexes = {};
function getScope(row, cfg, possibleScopes) {
	for(var i in possibleScopes) {
		var scopeName = possibleScopes[i];
		if(scopeName == "global") {
			return globalScope;
		} else if(row[scopeName]) {
			return getGroup(row, scopeName, cfg);
		}
	}
	return null;
}

function getGroup(row, id, cfg) {
	var groupCfg = cfg.groupTypes[id];
	var scope = getScope(row, cfg, groupCfg.scope);

	if(groupCfg.merge && row[id] == row[scope.type]) {
		return scope;
	}

	var displayName = substitute(groupCfg.displayName, merge(row, {
		name: row[id] || id + "-untitled",
		parent: scope.displayName
	}));

	var name = displayName;
	if(typeof scope.children[name] == "undefined") {
		var groupCfgId = groupCfg.id;
		if(typeof indexes[groupCfgId] == "undefined") {
			indexes[groupCfgId] = 0;
		}
		// get unique shortName:
		do {
			var index = ++indexes[groupCfgId];
			var shortName = substitute(groupCfg.shortName, {
				type: id,
				index: index,
				parent: scope.shortName
			});
		} while(groups.getGroup(shortName) != null);

		scope.children[name] = {
			shortName: shortName,
			displayName: displayName,
			type: groupCfgId,
			children: {}
		};
	}
	var group = scope.children[name];

	for(var i in groupCfg.parents) {
		var parentId = groupCfg.parents[i];
		scope = getGroup(row, cfg.groupTypes[parentId].id, cfg);
		if(typeof scope.children[name] == "undefined") {
			scope.children[name] = group;
		}
	}
	return group;
}

function getUser(row, id, cfg) {
	var name = row[id];
    if(!name) return null;

	var userCfg = cfg.userTypes[id];
	var scope = getScope(row, cfg, userCfg.scope);
	if(!scope) throw "User scope is not found for " + name;
    
	var user = {
		userName: name
	};

	scope.children[name] = user;
	return user;
}

function applyOrgstruct(scope, items, existingGroups, existingUsers, cfg) {

	for(var i in items) {
		var item = items[i];

		// a group
		if(item.displayName) {
			var groupCfg = cfg.groupTypes[item.type];

			logger.info("Importing group " + item.displayName);

			// get or create group:
			var group = existingGroups[item.displayName];
			if(!group) {
				group = groups.createRootGroup(item.shortName, item.displayName);
//				var groupNode = group.groupNode;
//				groupNode.properties["cm:name"] = item.shortName;
//				groupNode.save();
				existingGroups[group.displayName] = [ group ];
			} else if(group.length > 1) {
				throw "Found non-unique groups: " + item.displayName + " (" + group.length + " groups)";
			} else {
				group = group[0];
				item.shortName = group.shortName;
			}

			// ensure, that group has correct orgstruct type:
			var groupType = orgstruct.getGroupType(group);
			var groupSubtype = orgstruct.getGroupSubtype(group);
			if(groupType == null || groupType != groupCfg.groupType || groupSubtype != groupCfg.groupSubtype) {
				if(groupType != null) {
					orgstruct.convertToSimpleGroup(group.shortName);
				}
				orgstruct.createTypedGroup(groupCfg.groupType, groupCfg.groupSubtype, group.shortName);
			}

			// put into a scope group:
			if(scope && !groupContains(scope, group.fullName)) {
				scope.addAuthority(group.fullName);
			}

			// process all child groups:
			applyOrgstruct(group, item.children, existingGroups, existingUsers, cfg);

		} else if(item.userName) {

			// process user...

			logger.info("Importing user " + item.userName);
			var user = existingUsers[item.userName];

			if(user == null) {
				logger.warn("Couldn't find user " + item.userName);
				continue;
			}
			var userName = user.properties["cm:userName"];

			// put into a scope group:
			if (scope && !groupContains(scope, userName)) {
				logger.debug("Trying to add user " + item.userName + " to group " + scope.fullName);
				scope.addAuthority(userName);
			}
			else {
				logger.warn("Can not add user " + item.userName + " to group " + scope.fullName);
			}

		}
	}

}

function groupContains(group, fullName) {
	var group2 = groups.getGroupForFullAuthorityName(fullName);
	var parents = group2.allParentGroups;
	var groupFullName = group.fullName + "";
	for(var i in parents) {
		if(parents[i].fullName == groupFullName) {
			return true;
		}
	}
	return false;
}

(function() {

	var node = search.findNode(args.nodeRef);
	if(node == null) {
		status.setCode(status.STATUS_NOT_FOUND, "Can't find node " + args.nodeRef);
		return;
	}
	var from = parseInt(args.from);
	var to = parseInt(args.to);

	var cfg = readConfig();

	var data = new XML(node.content);
	var message = "";
	for(var i=from; i<to;i++){
		var row = data[cfg.recordName][i];
		if(!row) break;
		var obj = {};
		for(var j in cfg.groupTypes) {
			var groupType = cfg.groupTypes[j];
			obj[groupType.id] = row[groupType.xmlField] + "";
		}
		for(var j in cfg.userTypes) {
			var userType = cfg.userTypes[j];
			var id = row[userType.xmlField] + "";
			if(userType.caseInsensitive) {
			    id = id.toLowerCase();
			}
			obj[userType.id] = id;
		}

		for(var j in cfg.groupTypes) {
			var groupType = cfg.groupTypes[j];
			if(obj[groupType.id]) {
				getGroup(obj, groupType.id, cfg);
			}
		}

		for(var j in cfg.userTypes) {
			var userType = cfg.userTypes[j];
			getUser(obj, userType.id, cfg);
		}
	}

	// create all necessary group subtypes:
	for(var j in cfg.groupTypes) {
		var groupType = cfg.groupTypes[j];
		orgmeta.createSubType(groupType.groupType, groupType.groupSubtype);
	}

	existingGroups = getAllGroupsByDisplayName();
	existingUsers = getAllUsersById(cfg.userTypes.user);

	var scope = groups.getGroup("_orgstruct_home_");
	applyOrgstruct(scope, globalScope.children, existingGroups, existingUsers, cfg);

	model.message = message;
	model.groups = globalScope.children;
})();
