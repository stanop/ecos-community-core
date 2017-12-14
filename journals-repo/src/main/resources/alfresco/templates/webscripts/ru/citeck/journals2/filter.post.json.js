(function() {
	
	var title = json.get('title'),
		journalTypes = json.getJSONArray('journalTypes'),
		criteria = json.getJSONArray('criteria');
		
	if(!title) {
		status.setCode(status.STATUS_BAD_REQUEST, "Argument 'title' is required");
		return;
	}
	
	if(!journalTypes) {
		status.setCode(status.STATUS_BAD_REQUEST, "Argument 'journalTypes' is required");
		return;
	}
		
	var types = [];
	for(var i = 0, ii = journalTypes.length(); i < ii; i++) {
		types.push(journalTypes.get(i));
	}
	var filter = userhome.createNode(null, "journal:filter", {
		"cm:title": title,
		"journal:journalTypes": types
	});
	
	for(var i = 0, ii = criteria.length(); i < ii; i++) {
		var criterion = criteria.get(i),
			field = criterion.get('field'),
			predicate = criterion.get('predicate'),
			value = criterion.get('persistedValue');
		if(!field || !predicate) {
			throw "Illegal criterion (index " + i + "): field and predicate are required";
		}
		filter.createNode(null, "journal:criterion", {
			"journal:fieldQName": field,
			"journal:predicate": predicate,
			"journal:criterionValue": value
		}, "journal:searchCriteria");
	}
	
	model.filter = filter;
})();