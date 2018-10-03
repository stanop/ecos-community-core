(function() {
	var forceUpdate = args.forceUpdate || "false";
	var mode = args.mode || "diff";
	if(args.mode != "full" && args.mode != "diff") {
		status.setCode(status.STATUS_BAD_REQUEST, "Supported modes are full and diff");
		return;
	}
	
	var sync = services.get("userRegistrySynchronizer");
	sync.synchronize(forceUpdate=="true", mode == "full");
	
	model.code = status.getCode();
})();
