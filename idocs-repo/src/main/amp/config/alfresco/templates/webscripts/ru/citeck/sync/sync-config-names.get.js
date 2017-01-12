(function() {
	
	var service = services.get("ExternalSyncService");
	var configs = service.getConfigurationNames();
	
	model.configs = configs;

})();
