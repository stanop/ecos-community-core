<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary-v2/evaluator.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary-v2/filters.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary-v2/parse-args.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary-v2/doclist.lib.js">

/**
 * Method that performs the actual loading of the explicit nodes.
 *
 * @method doclist_getAllNodes
 * @param parsedArgs {Object}
 * @param filterParams {Object}
 * @param query {String}
 * @param totalItemCount {int}
 * @return {object} Returns the node and corresponding pagination metadata
 * {
 *    allNodes: {Array}
 *    totalRecords: {int}
 *    requestTotalCountMax: {int}
 *    paged: {boolean}
 *    query: {String}
 * }
 */
function doclist_getAllNodes(parsedArgs, filterParams, query, totalItemCount)
{
	var nodeRefs = null,
		totalRecords = 0,
		requestTotalCountMax = 0,
		paged = false,
		allNodes = [];

	for each (field in formdata.fields)
	{
		switch (String(field.name).toLowerCase())
		{
		case "nodeRefs":
			nodeRefs = "" + field.value;
			break;
		}
		if (nodeRefs)
			break;
	}
	
	if (!nodeRefs) {
		status.code = 400;
		status.message = "Required parameters are missing";
		status.redirect = true;
		formdata.cleanup();
	}

	var nodeRefsArr = nodeRefs.split(',');
	for (var i = 0; i < nodeRefsArr.length; i++) {
		var nodeRef = nodeRefsArr[i];
		var node = search.findNode(nodeRef);
		if (node)
			allNodes.push(node);
	}
	return {
		allNodes: allNodes,
		totalRecords: totalRecords,
		requestTotalCountMax: requestTotalCountMax,
		paged: paged,
		query: query
	};
}

/**
 * Document List Component: doclist
 */
model.doclist = doclist_main();
