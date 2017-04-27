(function() {
	var info = args;
	var components = sitedata.findComponents(info["scope"], info["region-id"], info["source-id"], null);
	model.components = components;
})();