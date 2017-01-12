
function getDictionary(nodes) {
	var dic = {};
	for(var i in nodes) {
		var name = nodes[i].name;
		dic[name] = nodes[i];
	}
	return dic;
}

function getSubTypeDictionary(type) {
	return getDictionary(orgmeta.getAllSubTypes(type));
}

function getAllSubTypeDictionaries() {
	var types = orgmeta.getGroupTypes();
	var dic = {};
	for(var i in types) {
		dic[types[i]] = getSubTypeDictionary(types[i]);
	}
	return dic;
}

function checkGroupType(type) {
	var types = orgmeta.getGroupTypes();
	for(var i in types) {
		if(types[i] == type) return true;
	}
	status.setCode(status.STATUS_NOT_FOUND, "Group type " + type + " is not known");
	return false;
}

function getGroupTypes() {
	var types = [];
	var typenames = orgmeta.getGroupTypes();
	for(var i in typenames) {
		types.push({
			name: typenames[i],
			root: orgmeta.getSubTypeRoot(typenames[i])
		});
	}
	return types;
}
	