(function() {

	var type = args.type;
	var sitePage = args.sitePage || "";
	var query;
	if (sitePage){
		query = 'TYPE:"' + type + '"' + ' AND @help\\:page:"' + sitePage  + '"';
	} else {
		query = 'TYPE:"' + type + '"' + ' AND ISNULL:help\\:page';
	}
	
	var maxItems = args.maxItems || 50;
	var skipCount = args.skipCount || 0;
	
	model.query = query;
	model.maxItems = maxItems;
	model.skipCount = skipCount;
	model.sitePage = sitePage;
	model.nodes = search.query({
		query: query,
		language: 'lucene',
		page: {
			maxItems: maxItems,
			skipCount: skipCount
		}
	});

})();