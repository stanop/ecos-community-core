var storeRefs = "workspace://SpacesStore,archive://SpacesStore".split(","),
	totalCount = 0;

for (var s = 0; s < storeRefs.length; s++) {
	var prevQuery = "",
		query = "";

	while (true) {
		query = 'ASPECT:"wfcf:hasConfirmDecisions"' + query;

		// to prevent unfinite loop:
		if (query == prevQuery)
			throw "Prevented unfinite loop. query=" + query;
		prevQuery = query;

		var results = search.luceneSearch(storeRefs[s], query, '@sys:node-dbid', true);
		if (results && results.length && results.length > 0) {
			totalCount = totalCount + results.length;
			for (var i = 0; i < results.length; i++) {
				var node = results[i],
					childAssocs = node.getChildAssocs(),
					cmContains = childAssocs["cm:contains"],
					packageContains = childAssocs["bpm:packageContains"];
				if (cmContains) {
					for (var j = 0; j < cmContains.length; j++) {
						var c = cmContains[j];
						c.addAspect('wfcf:confirmed');
						logger.log('Updated nodeRef=' + c.nodeRef);
					}
				}
				if (packageContains) {
					for (var j = 0; j < packageContains.length; j++) {
						var c = packageContains[j];
						c.addAspect('wfcf:confirmed');
						logger.log('Updated nodeRef=' + c.nodeRef);
					}
				}
			}

			var dbid = results[results.length - 1].properties["sys:node-dbid"] + 1;
			query = " AND @sys\\:node-dbid:[" + dbid + " TO MAX]";
		} else {
			break;
		}
	}
}
logger.log('Total tasks: ' + totalCount);
