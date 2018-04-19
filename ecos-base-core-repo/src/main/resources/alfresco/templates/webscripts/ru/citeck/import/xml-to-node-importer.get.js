<import resource="classpath:alfresco/templates/webscripts/ru/citeck/import/xni-webscripts-utils.js">
/**
* @author Roman Makarskiy
*/
(function () {
    var sort = {
        column: "@{http://www.alfresco.org/model/content/1.0}created",
        ascending: false
    };

    var def = {
        query: "TYPE:'xni:data'",
        store: "workspace://SpacesStore",
        language: "fts-alfresco",
        sort: [sort]
    };

    var xniData = search.query(def);
    var statusNode = search.findNode(GLOBAL_STATUS_NODEREF);

    if (!statusNode) {
        path = XNI_ROOT_PATH;
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