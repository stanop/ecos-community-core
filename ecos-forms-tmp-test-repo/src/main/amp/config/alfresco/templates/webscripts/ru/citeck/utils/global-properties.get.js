(function() {
	var globalProperties = services.get("global-properties");
	var name = args['name'];
	var data = [];
	if (name) {
		data.push( { key: name, value: '' + globalProperties[name] });
	}
	else {
		for (var key in globalProperties) {
			var keyValue = '' + key;
			data.push( { key: keyValue, value: '' + globalProperties[key] });
		}
	}
	model.data = data;
})();