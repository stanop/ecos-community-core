(function() {

    var username = person.properties.userName;
    var permissionService = services.get("permissionService");
    var sites = search.luceneSearch("TYPE:\"st:site\"");

    model.username = username;
    model.sites = [];

    for (var i in sites) {
        var site = sites[i];
        if (permissionService.hasPermission(site.nodeRef, "SiteConsumer")) {
            var siteResult = {};
            var siteName = site.properties["cm:name"];
            var journalLists = search.luceneSearch("TYPE:\"journal:journalsList\" AND @cm\\:name:\"site-" + siteName + "-main\" AND PARENT:\"workspace://SpacesStore/journal-meta-f-lists\"");
            if (journalLists && journalLists.length > 0) {
                var journalList = journalLists[0];
                var journals = journalList.assocs["journal:journals"];
                var journalResult = [];
                if (journals && journals.length > 0) {
                    for (var j in journals) {
                        var journal = journals[j];
                        if (permissionService.hasPermission(journal.nodeRef, "Read")) {
                            journalResult.push(journal);
                        }
                    }
                }
                siteResult.site = site;
                siteResult.journals = journalResult;
                model.sites.push(siteResult);
            }
        }
    }
})();