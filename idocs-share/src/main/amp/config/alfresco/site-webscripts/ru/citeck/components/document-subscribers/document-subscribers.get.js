(function() {

	var nodeRef = args.nodeRef;
	var assocType = args.assocType;
	if(!nodeRef) {
		return;
	}
	model.nodeRef = nodeRef;
	model.assocType = assocType;
	model.currentUserId = user.id;

})();