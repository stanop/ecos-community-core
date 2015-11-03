(function() {
	var result = false;
	var nodeRefStr = args['nodeRef'];
	var aspectStr = args['aspect'];
	if (nodeRefStr && aspectStr) {
		var node = search.findNode(nodeRefStr);
		if (node && node.hasAspect(aspectStr))
			result = true;
	}
	model.data = result ? "true" : "false";
})();
