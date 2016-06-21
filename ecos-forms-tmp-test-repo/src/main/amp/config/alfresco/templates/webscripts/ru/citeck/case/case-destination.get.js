(function() {

	// checks input parameters
	if(!args.caseNode) {
		status.setCode(status.STATUS_BAD_REQUEST, "Case node is not specified");
		return;
	}
	if(!args.configName) {
		status.setCode(status.STATUS_BAD_REQUEST, "Config name is not specified");
		return;
	}

	model.data = caseService.destination(args.caseNode, args.configName);

})();
