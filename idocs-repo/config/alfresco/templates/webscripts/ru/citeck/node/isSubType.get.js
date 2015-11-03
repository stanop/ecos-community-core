function isSubType(nodeRef, type) {
	var node = search.findNode(nodeRef);
	return node && node.isSubType(type);
}

function isSubTypes(nodeRef, types) {
	var result = false;
	var node = search.findNode(nodeRef);
	if (node) {
		var ta = types.split(',');
		for (var i = 0; i < ta.length; i++) {
			if (node.isSubType(ta[i])) {
				result = true;
				break;
			}
		}
	}
	return result;
}

function main() {
	var nodeRef = args['nodeRef'];
	var types = args['types'];
	var type = args['type'];

	var data = "false";
	if (nodeRef && type)
		data = isSubType(nodeRef, type) ? "true" : "false";
	else if (nodeRef && types)
		data = isSubTypes(nodeRef, types) ? "true" : "false";
	model.data = data;
}

main();
