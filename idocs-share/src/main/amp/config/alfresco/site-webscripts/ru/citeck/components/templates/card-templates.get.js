<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function getCardTemplateTypes() {
    var connector = remote.connect("alfresco");
    var url = "/citeck/templates/card-template-types";
    var result = connector.call(url);
    if (result.status != 200){
        AlfrescoUtil.error(result.status, 'Could not load card template types: request url = ' + url);
    }
    return eval('(' + result + ')');
}

(function() {
    AlfrescoUtil.param('nodeRef');
    AlfrescoUtil.param('site', null);
    model.nodeRefExists = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
    if (model.nodeRefExists) {
        var types = getCardTemplateTypes();
        var connector = remote.connect("alfresco");
        var url = "/citeck/templates/card-templates?nodeRef=" + args.nodeRef;
        var result = connector.call(url);
        if (result.status != 200){
            AlfrescoUtil.error(result.status, 'Could not load card templates: request url = ' + url);
        }
        var templates = eval('(' + result + ')');
        for (var i in templates) {
            for (var j in types) {
                if (templates[i].type == types[j].name) {
                    templates[i].typeTitle = types[j].title;
                }
            }
        }
        model.templates = templates;
    }
})();