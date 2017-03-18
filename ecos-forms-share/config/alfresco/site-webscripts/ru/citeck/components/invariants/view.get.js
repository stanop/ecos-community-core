<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/invariants/view.lib.js">
<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/citeck.lib.js">


(function() {
    model.isMobile = isMobileDevice(context.headers["user-agent"]);

    var viewData = getViewData(args);
    if(!viewData) return;

    var view = viewData.view,
        attributes = getAttributes(view);

    if (model.isMobile) {
        for (var a = 0; a < attributes.length; a++) {
            prepareAttributeForMobileVersion(attributes[a]);
        }
    }

    attributes = map(attributes, function(attr) { return attr.attribute; });

    var invariantSet = { invariants: [], classNames: [] }, viewScopedInvariants = [];
    if (view.params.postloadInvariants != "true") {
        invariantSet = getInvariantSet(args, attributes),
        viewScopedInvariants = getViewScopedInvariants(view);
    }

    
    // ATTENTION: this view model should comply to repository NodeView interface!
    var defaultModel = {},
        publicViewProperties = [ 'class', 'id', 'kind', 'mode', 'template', 'params' ];

    if (invariantSet.model) {
        for(var name in invariantSet.model) { defaultModel[name] = invariantSet.model[name]; }
    }
      
    defaultModel.view = {};
    for(var i in publicViewProperties) {
        var name = publicViewProperties[i];
        defaultModel.view[name] = view[name];
    }


    if (view.template == "tabs") {
        var groups = get(view, "view"),
        attributesByGroups = map(groups, function(group) {
            return map(getAttributes(group), function(attribute) { return attribute.attribute; })
        });

        // generate id
        each(groups, function(group, index) {
            group["genId"] = view["class"].replace(":", "_") + "-group-" + index;
        });

        var invariantSetByGroups = map(attributesByGroups, function(group) { return getInvariantSet(args, group) }),
            viewScopedInvariantsByGroups = map(groups, function(group) { return getViewScopedInvariants(group) }),
            invariantsByGroups = [];
            
        for (var fi = 0; fi < groups.length; fi++) {
            invariantsByGroups.push(viewScopedInvariantsByGroups[fi].concat(invariantSetByGroups[fi].invariants));
        }

        model.attributesByGroups = attributesByGroups;
        model.invariantsByGroups = invariantsByGroups;
        model.groups = map(groups, function(group, index) {
            return {
                "id": group.id || group.genId,
                "index": index,
                "attributes": attributesByGroups[index],
                "invariants": invariantsByGroups[index]
            }
        });
    };

    model.view = view;
    model.canBeDraft = viewData.canBeDraft;

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