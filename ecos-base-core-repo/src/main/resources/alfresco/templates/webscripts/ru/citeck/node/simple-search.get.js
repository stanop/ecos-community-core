(function() {

	var type = args.type;
	var pathPart = "";
	var maxRetries = args.maxRetries || 1;
	if(args.site) {
		var site = siteService.getSite(args.site);
		if(site == null) {
			status.setCode(status.STATUS_NOT_FOUND, "Site " + args.site + " not found");
			return;
		}
		pathPart = ' AND PATH:"' + site.node.qnamePath + '//*"';
	}
	var propertyPart = "";
	if (args.property && args.value) {
		var props = args.property.split(',');
		propertyPart = " AND (";
		for (var i = 0; i < props.length; i++) {
			propertyPart += "@" + props[i].replace(":", "\\:") + ":\"" + args.value +"\"";
			if (i < props.length - 1)
				propertyPart += " OR ";
		}
		propertyPart += ")";
	}
	var textPart = "";
	if (args.text) {
	    textPart = "AND (@cm\\:title:\"" + args.text + "\" OR @cm\\:name:\""+args.text+"\")";
	}
	var typePart = 'TYPE:"' + type + '"';
	var query = typePart + pathPart + propertyPart + textPart;
	var maxItems = args.maxItems || 50;
	var skipCount = args.skipCount || 0;
	
	model.query = query;
	model.maxItems = maxItems;
	model.skipCount = skipCount;
	for (var i = 0; i < maxRetries; i++) {
		try {
			model.nodes = search.query({
				query: query,
				language: 'lucene',
				page: {
					maxItems: maxItems,
					skipCount: skipCount
				}
			});
		}
		catch(e) {
			// retrying...
			logger.error('Retrying simple search' + e);
		}
	}

})();
