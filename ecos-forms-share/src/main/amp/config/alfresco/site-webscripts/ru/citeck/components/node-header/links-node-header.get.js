<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function main() {
    AlfrescoUtil.param("nodeRef");
    AlfrescoUtil.param("site", null);
    AlfrescoUtil.param("showPath", "true");

    var rootNode = model.site ? "&rootNodePathQname=" + "/app:company_home/st:sites/cm:" + model.site + "/cm:documentLibrary" : "";
    var url = "/citeck/node/path/details?nodeRef=" + model.nodeRef + rootNode;
    var result = remote.call(url);
    if (result.status != 200) {
        AlfrescoUtil.error(result.status, 'Could not load node path: request url = ' + url);
    }
    model.path = eval('(' + result + ')').path;
    var storeType = model.nodeRef.substring(0, model.nodeRef.indexOf("://"));
    if (storeType == "archive") {
        model.locationType = "archive"
    } else if (model.site != null) {
        model.locationType = "documents"
    } else {
        model.locationType = "repository"
    }
}

main();
