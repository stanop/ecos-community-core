function getView(type) {
    var response = remote.call('/citeck/invariants/view?type=' + type);
    return eval('(' + response + ')');
}

function getAttributes(view) {
    var attributes = [];
    getAttributesRecursively(view, attributes);
    return attributes;
}

function getAttributesRecursively(element, attributes) {
    if(element.type == "view") {
        for(var i in element.elements) {
            getAttributesRecursively(element.elements[i], attributes);
        }
    } else if(element.type == "field") {
        if(attributes.indexOf(element.attribute) == -1) {
            attributes.push(element.attribute);
        }
    }
}

function getInvariantSet(type, attributes) {
    var response = remote.call('/citeck/invariants?type=' + type + 
            (attributes && attributes.length ? "&attributes=" + attributes.join(',') : ''));
    return eval('(' + response + ')');
}

function getViewScopedInvariants(view) {
    var invariants = [];
    getInvariantsRecursively(view, invariants);
    return invariants
}

function getInvariantsRecursively(element, invariants) {
    if(element.type == "view") {
        for(var i in element.elements) {
            getInvariantsRecursively(element.elements[i], invariants);
        }
    } else if(element.type == "field" && element.invariants && element.invariants.length) {
        for(var i in element.invariants) {
            invariants.push(element.invariants[i]);
        }
    }
}

(function() {
    
    var type = args.type || 'pass:passport';
    var response = remote.call('/citeck/invariants/view?type=' + type);
    var view = eval('(' + response + ')');
    
    var attributes = getAttributes(view);
    var invariantSet = getInvariantSet(type, attributes);
    var viewScopedInvariants = getViewScopedInvariants(view);
    
    model.type = type;
    model.view = view;
    model.attributes = attributes;
    model.invariants = viewScopedInvariants.concat(invariantSet.invariants);
    model.defaultModel = invariantSet.model;
    
})()