(function() {
	var nodeRef = args.nodeRef;
	
	if(!nodeRef) {
		status.setCode(status.STATUS_BAD_REQUEST, "Parameter nodeRef should be specified");
		return;
	}
	
	var node = search.findNode(nodeRef);
	
	if (!node) {
		status.setCode(status.STATUS_NOT_FOUND, "Can not find node " + nodeRef);
		return;
	}
	
	if (!node.hasPermission("Delete")) {
		status.setCode(status.STATUS_FORBIDDEN, "You do not have permission to delete node " + nodeRef);
		return;
	}
	
	node.remove();

	model.success = true;
})();
