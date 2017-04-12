(function () {
    var xniData = search.luceneSearch('TYPE\:"xni:data"');
    var statusNode = search.findNode("workspace://SpacesStore/xni-parser-status");

    if (!statusNode) {
        path = "/app:company_home/app:dictionary/cm:xni-data";
        var folder = search.selectNodes(path)[0];
        var properties = [];
        properties['sys:node-uuid'] = "xni-parser-status";
        properties['xni:prsStatus'] = "Wait";
        statusNode = folder.createNode("xni-parser-status", "xni:parserStatus", properties);
    }

    if (statusNode) {
        model.parserStatus = statusNode;
    }

    if (xniData.length > 0) {
        model.xmlData = xniData;
    } else {
        model.xmlData = [];
    }
})();