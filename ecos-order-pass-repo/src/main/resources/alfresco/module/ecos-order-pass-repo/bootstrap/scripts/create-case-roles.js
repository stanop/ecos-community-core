function createCaseRoles() {
    path= "/app:company_home/app:dictionary/cm:dataLists/cm:case-role";
    var folder = search.selectNodes(path)[0];
    var alreadyLoadedItems = folder.getChildren();

    var alreadyLoaded = [];
    for (var i in alreadyLoadedItems) {
        alreadyLoaded.push(alreadyLoadedItems[i].properties["icaseRole:varName"]);
    }

    for (var i in data) {
        var caseRole = data[i];
        if(alreadyLoaded.indexOf(caseRole.name, 0)<0) {
            var properties = [];
            properties['icaseRole:varName'] = caseRole.name;
            properties['icaseRole:isReferenceRole'] = true;

            if (caseRole.title) {
                properties['cm:title'] = caseRole.title;
            }

            var createdRole = folder.createNode(null, "icaseRole:role", properties);

            if (caseRole.titleRu && caseRole.titleRu) {
                utils.setLocale("en");
                createdRole.properties["cm:title"] = caseRole.titleEn;
                createdRole.save();

                utils.setLocale("en_US");
                createdRole.properties['cm:title'] = caseRole.titleEn;
                createdRole.save();

                utils.setLocale("ru_RU");
                createdRole.properties['cm:title'] = caseRole.titleRu;
                createdRole.save();
            }
        }
    }
}