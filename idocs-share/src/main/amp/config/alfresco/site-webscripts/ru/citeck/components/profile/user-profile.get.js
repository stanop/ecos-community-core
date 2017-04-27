<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/invariants/view.lib.js">
<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/citeck.lib.js">
<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

(function() {
    var username = page.url.templateArgs["userid"];
    if (!username || username == null) {
        username = user.id;
    }

    var connector = remote.connect("alfresco");
    var url = "/api/orgstruct/authority/" + encodeURIComponent(username);
    var result = connector.call(url);
    if (result.status != 200) {
        AlfrescoUtil.error(result.status, 'Could not load user: request url = ' + url);
    }
    var currentUser = eval('(' + result + ')');

    connector = remote.connect("alfresco");
    url = '/citeck/has-permission?nodeRef=' + currentUser.nodeRef + '&permission=Write';
    result = connector.call(url);
    if (result.status != 200) {
        AlfrescoUtil.error(result.status, 'Could not check permissions for user: request url = ' + url);
    }
    var permissionResult = eval('(' + result + ')');

    model.userRef = currentUser.nodeRef;
    model.writeMode = (permissionResult == true || permissionResult == "true");
    model.mode = (args.mode && model.writeMode) ? args.mode : "view";

    var viewArgs = {
        nodeRef: currentUser.nodeRef,
        mode: model.mode
    };

    var viewData = getViewData(viewArgs);
    if(viewData == null) return;
    var view = viewData.view;

    var attributes = map(getAttributes(view), function(attr) { return attr.attribute; });
    var invariantSet = getInvariantSet(view['class'], attributes);
    var viewScopedInvariants = getViewScopedInvariants(view);
    var defaultModel = {};
    for (var name in invariantSet.model) {
        defaultModel[name] = invariantSet.model[name];
    }
    var publicViewProperties = ['class', 'id', 'kind', 'mode', 'template', 'params'];
    // ATTENTION: this view model should comply to repository NodeView interface!
    defaultModel.view = {};
    for (var i in publicViewProperties) {
        var name = publicViewProperties[i];
        defaultModel.view[name] = view[name];
    }

    model.view = view;
    model.attributes = attributes;
    model.invariants = viewScopedInvariants.concat(invariantSet.invariants);
    model.defaultModel = defaultModel;
})();