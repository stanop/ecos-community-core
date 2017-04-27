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
	
    model.filter = filter;
})();