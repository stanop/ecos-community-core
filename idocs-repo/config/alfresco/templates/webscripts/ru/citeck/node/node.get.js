(function() {
	var extractNames = function(values) {
		var result = [];
		if (values) {
			values = ('' + values).replace(/^\s+|\s+$/g, '');
			var valuesArr = values.split(',');
			for (var i = 0; i < valuesArr.length; i++) {
				var valueName = valuesArr[i].replace(/^\s+|\s+$/g, '');
				if (valueName)
					result.push(valueName);
			}
		}
		return result;
	}

	var nodeRef = args['nodeRef'],
		props = args['props'],
		assocs = args['assocs'],
		childAssocs = args['childAssocs'],
		propsResult = [],
		assocsResult = [],
		childAssocsResult = [],
		siteShortName = null;

	if (nodeRef) {
		var node = search.findNode(nodeRef);
		if (node) {
			model.node = node;
			var propsArr = [],
				assocsArr = [],
				childAssocsArr = [];
			siteShortName = node.siteShortName;
			if (props || assocs || childAssocs) {
				// Returns only specified properties and child associations
				propsArr = extractNames(props);
				assocsArr = extractNames(assocs);
				childAssocsArr = extractNames(childAssocs);
			}
			else {
				// We are going to return all properties and child associations
				for (var prop in node.properties)
					propsArr.push(prop);
				for (var assoc in node.assocs)
					assocsArr.push(assoc);
				for (var childAssoc in node.childAssocs)
					childAssocsArr.push(childAssoc);
			}
			for (var i = 0; i < propsArr.length; i++) {
				var prop = propsArr[i];
				propsResult.push({
					key: utils.shortQName(prop),
					value: node.properties[prop]
				});
			}
			for (var i = 0; i < assocsArr.length; i++) {
				var assoc = assocsArr[i];
				var assocObj = node.assocs[assoc];
				if (assocObj) {
					var nodeRefs = [];
					if (assocObj.length > 0) {
						for (var j = 0; j < assocObj.length; j++)
							nodeRefs.push(assocObj[j].nodeRef.toString());
					}
					assocsResult.push({
						key: utils.shortQName(assoc),
						value: nodeRefs
					});
				}
			}
			for (var i = 0; i < childAssocsArr.length; i++) {
				var assoc = childAssocsArr[i];
				var assocObj = node.childAssocs[assoc];
				if (assocObj) {
					var nodeRefs = [];
					if (assocObj.length > 0) {
						for (var j = 0; j < assocObj.length; j++)
							nodeRefs.push(assocObj[j].nodeRef.toString());
					}
					childAssocsResult.push({
						key: utils.shortQName(assoc),
						value: nodeRefs
					});
				}
			}
		}
	}
	model.props = propsResult;
	model.assocs = assocsResult;
	model.childAssocs = childAssocsResult;
	model.siteShortName = siteShortName;
})();
