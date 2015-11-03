(function() {
	/**
	 * It binds the new added event with case element and moves this event
	 * to the global event folder of the current site.
	 * @param document - added event to the case element
	 * @param space - folder in the case element
	 * @param companyhome - company home
	 */
	var caseNode = space.getParent();
	if (caseNode.hasAspect('icase:case')) {
		var shortSiteName = caseNode.getSiteShortName(),
			siteName = 'cm:' + shortSiteName,
			site = siteService.getSite(shortSiteName);
		if (site) {
			var folder = site.getContainer('calendar');
			if (!folder)
				folder = site.createContainer('calendar');
			if (document.addAspect('icase:relatedCases')) {
				document.createAssociation(caseNode, 'icase:relatedCases');
				document.move(folder);
			}
		}
	}
})();
