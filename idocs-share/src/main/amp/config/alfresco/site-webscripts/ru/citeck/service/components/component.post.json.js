(function() {

	var clientRequest = json.toString();
	var info = eval('(' + clientRequest + ')');

	// unbind old matching components
	var oldComponents = sitedata.findComponents(info["scope"], info["region-id"], info["source-id"], null);
	for(var i in oldComponents) {
		sitedata.unbindComponent(info["scope"], info["region-id"], info["source-id"]);
	}

	// create new component
	var newComponent = sitedata.newComponent(info["scope"], info["region-id"], info["source-id"]);
	for(var i in info) {
		newComponent.properties[i] = info[i];
	}
	newComponent.save();

	model.oldComponents = oldComponents;
	model.newComponent = newComponent;
})();