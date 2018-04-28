<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/invariants/view.lib.js">
<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/citeck.lib.js">

(function() {
    model.isMobile = isMobileDevice(context.headers["user-agent"]);

    var viewData = getViewData(args);
    if (!viewData) return;

    var view = viewData.view,
        attributeSet = getAttributeSet(args, view),
        attributes = getAttributes(view), 
        attributeNames = map(attributes, function(attr) { return attr.attribute; }),
        attributesInfo = map(attributes, function (attr) {
            var params = attr.params || {};
            return {
                name: attr.attribute,
                type: attr.fieldType,
                nodetype: attr.nodetype,
                datatype: attr.datatype,
                javaclass: params.javaclass || attr.javaclass
            };
        }),
        viewNodeRef = viewData.nodeRef;

    var writePermission = false,
        inlineEdit = false,
        viewMode = view.mode == "view";

    if (viewNodeRef) {

        writePermission = getWritePermission(viewNodeRef);

        if (writePermission && viewMode) {
            args.inlineEdit = view.params.inlineEdit;
            
            if (view.params.inlineEdit) {
                inlineEdit = view.params.inlineEdit == "true"; 
            } else { inlineEdit = true; }
        }
    }

    model.writePermission = writePermission;
    model.inlineEdit = inlineEdit;

    if (model.isMobile) {
        for (var a = 0; a < attributes.length; a++) {
            prepareAttributeForMobileVersion(attributes[a]);
        }
    }

    var defaultModel = {},
        publicViewProperties = [ 'class', 'id', 'kind', 'mode', 'template', 'params' ];
     
    defaultModel.view = {};
    for(var i in publicViewProperties) {
        var name = publicViewProperties[i];
        defaultModel.view[name] = view[name];
    }

    var invariantSet = { invariants: [], classNames: [] }, viewScopedInvariants = [];
    if (view.params.preloadInvariants == "true") {
        invariantSet = getInvariantSet(args, attributeNames),
        viewScopedInvariants = getViewScopedInvariants(view);

        if (invariantSet.model) {
            for(var name in invariantSet.model) { defaultModel[name] = invariantSet.model[name]; }
        }

        model.invariants = viewScopedInvariants.concat(invariantSet.invariants);
        model.classNames = invariantSet.classNames;
    }
   
    model.viewNodeRef = viewNodeRef ? viewNodeRef : null;
    model.view = view;
    model.defaultModel = defaultModel;
    model.formKey = viewData.formKey;
    model.formType = viewData.formType;

    model.attributeSet = attributeSet;
    model.attributeNames = attributeNames;
    model.attributesInfo = attributesInfo;

    model.canBeDraft = viewData.canBeDraft;

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