<import resource="classpath:alfresco/site-webscripts/ru/citeck/components/header/header-tokens.lib.js">

(function() {
	var tokens = {};

	// insert lastsite into tokens
	var lastsite = getLastSiteFromCookie();
	tokens.lastsite = lastsite;
	
	// save tokens
	model.tokens = tokens;

})();