<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/invariants/view.get.js">

(function () {

    var refreshArgKeys = ['nodeRef', 'htmlid', 'type', 'viewId', 'mode', 'nodeRefAttr', 'style'];
    var refreshArgs = {};

    for (var i in refreshArgKeys) {
        var key = refreshArgKeys[i];
        if (args[key]) {
            refreshArgs[key] = args[key];
        }
    }

    model.refreshArgs = refreshArgs;

})();
