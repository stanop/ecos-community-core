(function() {

	var service = services.get("syncServiceRegistry");
	var configs = service.getConfigurationNames();
	
	model.configs = configs;

})();
