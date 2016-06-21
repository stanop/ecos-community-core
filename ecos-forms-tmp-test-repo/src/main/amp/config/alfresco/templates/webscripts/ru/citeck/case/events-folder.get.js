(function() {

	var caseNode = search.findNode(args.caseNode);
	if(!caseNode) {
		status.setCode(status.STATUS_NOT_FOUND, "The case node '" + args.caseNode + "' is not found");
		return;
	}

	var folderName = "events";
	var folder = caseNode.childByNamePath(folderName);
	if(!folder) {
		status.setCode(status.STATUS_NOT_FOUND, "Can not find folder by name '" + folderName + "'");
		return;
	}

	model.data = folder.nodeRef.toString();

})();
