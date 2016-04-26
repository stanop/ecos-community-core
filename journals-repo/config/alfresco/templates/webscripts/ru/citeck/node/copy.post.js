(function() {

	var sourceNode = search.findNode(args.source);
	if(!sourceNode) {
		status.setCode(status.STATUS_NOT_FOUND, "Can not find node " + args.source);
		return;
	}
	
	var destination = search.findNode(args.destination);
	if(!destination) {
		status.setCode(status.STATUS_NOT_FOUND, "Can not find node " + args.destination);
		return;
	}
	
	var copiedNode = null;
	if(sourceNode.isContainer) {
		var deepCopy = args.deep != "false";
		copiedNode = sourceNode.copy(destination, deepCopy);
	} else {
		copiedNode = sourceNode.copy(destination);
	}
	
	model.sourceNode = sourceNode;
	model.destination = destination;
	model.copiedNode = copiedNode;
	
})();
