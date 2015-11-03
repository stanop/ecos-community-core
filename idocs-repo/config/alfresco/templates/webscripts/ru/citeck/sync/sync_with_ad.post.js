(function() {

	var forceUpdate = args.forceUpdate || "false";
	var mode = args.mode || "diff";
	if(args.mode != "full" && args.mode != "diff") {
		status.setCode(status.STATUS_BAD_REQUEST, "Supported modes are full and diff");
		return;
	}
	
	var sync = services.get("Synchronization");
	var bean = sync.getApplicationContext().getBean("userRegistrySynchronizer");

	bean.synchronize(forceUpdate=="true", mode == "full", true);
	
	model.code = status.getCode();

})();
