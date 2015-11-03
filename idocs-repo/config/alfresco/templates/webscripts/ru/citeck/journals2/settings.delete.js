(function() {
    var nodeRef = args.nodeRef;
    if (!nodeRef) {
        status.setCode(status.STATUS_BAD_REQUEST, "Argument nodeRef has not been provided.");
        return;
    }

    var settings = search.findNode(nodeRef);
    if (!settings) {
        status.setCode(status.STATUS_NOT_FOUND, "Settings " + nodeRef + " not found");
        return;
    }
	
    if (!settings.hasPermission("Delete")) {
        status.setCode(status.STATUS_FORBIDDEN, "You are not allowed to delete settings " + nodeRef);
        return;
    }
	
    settings.remove();
	
    model.nodeRef = nodeRef;
    model.success = true;
})();
