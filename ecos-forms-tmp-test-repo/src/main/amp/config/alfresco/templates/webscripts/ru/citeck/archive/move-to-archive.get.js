(function() {
	var result = false,
		nodeRef = args['nodeRef'],
		cause = args['cause'];
	if (nodeRef) {
		var node = search.findNode(nodeRef);
		if (node !== null) {
			archiveService.move('' + node.getNodeRef(), cause);
			result = true;
		}
	}
	model.data = result ? 'true' : 'false';
})();
