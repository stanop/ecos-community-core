(function() {
	var nodeRef = args['nodeRef'],
		contentData = args['contentData'];

	if (nodeRef) {
		var node = search.findNode(nodeRef),
			properties = [],
			contentDataProperties = [];
		if (node) {
			model.data = node;

			for (var p in node.properties) {
				var key = utils.shortQName(p).split(":")[1];

				if (key == "content") {
					var contentData = node.properties[p];
					contentDataProperties.mimetype = contentData.mimetype;
					contentDataProperties.encoding = contentData.encoding;
					contentDataProperties.size = contentData.size;
					continue;
				}

				properties.push({
					key: key,
					value: node.properties[p]
				});
			}

			model.properties = properties;
		} 
	}

	if (contentData) {
		model.contentDataProperties = contentDataProperties;
	}
})();
