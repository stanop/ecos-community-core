<import resource="classpath:alfresco/site-webscripts/ru/citeck/utils/config-conditions.js">

function getViewOnly() {

    var onSiteOnly = [];
    try {
        onSiteOnly = config.scoped["DocsCreatePermissions"]["documentTypes"].childrenMap["on-site-only"];
    } catch (e) {
        return false;
    }
    var withoutCreateActions = false;
    if (onSiteOnly && onSiteOnly.size() > 0 && page.url.uri.indexOf("/site/") == -1) {
        withoutCreateActions = true;
    }

    return withoutCreateActions;
}

function getHiddenDocTypes() {
    var docTypes = [];
    var onSiteOnly = [];
    try {
        var docTypesXML = config.scoped["DocsCreatePermissions"]["documentTypes"].childrenMap["hidden-documentType"];
        if (!docTypesXML) {
            return [];
        }
    } catch (e) {
        return [];
    }

    var evaluator = new CompositeEvaluator();
    evaluator.registerEvaluator(new GroupEvaluator());
    evaluator.registerEvaluator(new UserEvaluator());

    for (var i = 0, il = docTypesXML.size(); i < il; i++)
    {
        var a = docTypesXML.get(i);
        if(evaluator.evaluate(a))
        {
            docTypes.push(a.attributes.name);
        }
    }
    return docTypes;
}