<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/invariants/view.lib.js">

(function() {

    var view = getView(args);
    if(view == null) return;

    var attributes = getAttributes(view);
    var invariantSet = getInvariantSet(view['class'], attributes);
    var viewScopedInvariants = getViewScopedInvariants(view);
    var defaultModel = {};
    for(var name in invariantSet.model) {
        defaultModel[name] = invariantSet.model[name];
    }
    var publicViewProperties = [ 'class', 'id', 'kind', 'mode', 'template', 'params' ];
    // ATTENTION: this view model should comply to repository NodeView interface!
    defaultModel.view = {};
    for(var i in publicViewProperties) {
        var name = publicViewProperties[i];
        defaultModel.view[name] = view[name];
    }

    model.view = view;
    model.attributes = attributes;
    model.invariants = viewScopedInvariants.concat(invariantSet.invariants);
    model.defaultModel = defaultModel;

})()