function filterAndSort(cardlets, column) {
	var filteredCardlets = [];
	for(var i in cardlets) {
		if(cardlets[i].regionColumn == column) {
			filteredCardlets.push(cardlets[i]);
		}
	}
	return filteredCardlets.sort(function(a, b) {
		return a.regionPosition < b.regionPosition ? -1
				: a.regionPosition > b.regionPosition ? 1 
				: 0;
	});
}

(function() 
{
	// get case element configs
	var result = remote.call("/citeck/cardlets?nodeRef=" + page.url.args.nodeRef + "&mode=" + (page.url.args.mode || ""));
	if(result.status == 200) {
		var cardlets = eval('(' + result + ')').cardlets;
	} else {
		var cardlets = [];
	}
	
	model.topRegions = filterAndSort(cardlets, "top");
	model.leftRegions = filterAndSort(cardlets, "left");
	model.rightRegions = filterAndSort(cardlets, "right");
	model.bottomRegions = filterAndSort(cardlets, "bottom");
})();
