(function() {
	var nodeRef = args.nodeRef;
	if(!nodeRef) {
		status.setCode(status.STATUS_BAD_REQUEST, "Argument 'nodeRef' must be specified");
		return null;
	}
	var node = search.findNode(nodeRef);
	if(!node) {
		status.setCode(status.STATUS_NOT_FOUND, "Can not find node: " + nodeRef);
		return null;
	}
	
	model.cardmodes = cardlets.queryCardModes(node);
	model.document = node;

})();