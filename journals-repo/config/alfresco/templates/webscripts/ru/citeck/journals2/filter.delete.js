(function() {
    var nodeRef = args.nodeRef;
    if (!nodeRef) {
        status.setCode(status.STATUS_BAD_REQUEST, "Argument nodeRef has not been provided.");
        return;
    }

    var filter = search.findNode(nodeRef);
	if (!filter) {
        status.setCode(status.STATUS_NOT_FOUND, "Filter " + nodeRef + " not found");
        return;
	}
	
	if (!filter.hasPermission("Delete")) {
        status.setCode(status.STATUS_FORBIDDEN, "You are not allowed to delete filter " + nodeRef);
        return;
	}
	
	filter.remove();
	
	model.nodeRef = nodeRef;
    model.success = true;
})();