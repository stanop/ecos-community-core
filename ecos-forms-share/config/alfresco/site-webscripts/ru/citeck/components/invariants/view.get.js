<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/invariants/view.lib.js">
<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/citeck.lib.js">


(function() {
    model.isMobile = isMobileDevice(context.headers["user-agent"]);

    var view = getView(args);
    if(view == null) return;

    var attributes = getAttributes(view);
    if (model.isMobile) {
        for (var a = 0; a < attributes.length; a++) {
            prepareAttributeForMobileVersion(attributes[a]);
        }
    }
    attributes = map(attributes, function(attr) { return attr.attribute; });

    var invariantSet = getInvariantSet(args, attributes);
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
    model.classNames = invariantSet.classNames;
    model.defaultModel = defaultModel;

    if (args.nodeRef) model.writePermission = getWritePermission(args.nodeRef);

})()


function prepareAttributeForMobileVersion(attribute) {
    var journalRegion = findBy(attribute.regions, "template", function(template) {
            return /^(select-|)journal$/.test(template);
        });

    if (journalRegion) {
        // change 'journal' or 'select-journal' to 'autocomplete'
        journalRegion.template = "autocomplete";

        // delete view region
        var viewRegion = findBy(attribute.regions, "template", "view");
        if (viewRegion) attribute.regions.splice(attribute.regions.indexOf(viewRegion), 1);
    }
}