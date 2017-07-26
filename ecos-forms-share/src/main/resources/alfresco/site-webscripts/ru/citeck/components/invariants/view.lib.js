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
        if(attributes.indexOf(element) == -1) {
            attributes.push(element);
        }
    }
}

function get(view, type, template) {
    var objects = [];
    for(var i in view.elements) {
        if(view.elements[i].type == type && objects.indexOf(view.elements[i]) == -1) { 
            objects.push(view.elements[i]); 
        }
    }
    return objects;
}

function getAttributeSet(args, view) {
    var attributeSet = { attributes: [], sets: [], id: "", template: view.template };
    setAttributeSetId(view);

    for (var i in view.elements) {
        var element = view.elements[i];
        if (element.type == "field") {
            attributeSet.attributes.push({ name: element.attribute, template: element.template });
        } else if (element.type == "view" && element.template.indexOf("set") != -1) {
            attributeSet.sets.push(getAttributeSet(args, element));
        }
    }

    attributeSet.id = view.id || (function() {
        var identificator = attributeSet.attributes.map(function(attr) { return attr.name; }).join("_");
        identificator += "_" + attributeSet.attributes.length + "_" + attributeSet.sets.length;
        return identificator;
    })();

    // TODO: replace many requests for one (speed up!!!)
    var invariantSet = getInvariantSet(args, attributeSet.attributes.map(function(attr) { return attr.name; })) || [],
        viewScopeInvariants = getViewInvariants(view) || [];
    attributeSet.invariants = invariantSet.invariants.concat(viewScopeInvariants);

    return attributeSet;
}

function setAttributeSetId(view) {
    var attributes = [], setsCount = 0;
    for (var i in view.elements) {
        if (view.elements[i].type == "field") { attributes.push(view.elements[i].attribute); }
        else if (view.elements[i].type == "view" && view.template.indexOf("set") != -1) { 
            buildAttributeSetId(view.elements[i]);
            setsCount++;
        }
    }

    view.params.setId = attributes.join("_") + "_" + attributes.length + "_" + setsCount;
}

function getViewInvariants(view) {
    var invariants = [];
    view.elements.forEach(function(element) {
        if (element.type == "field" && element.invariants && element.invariants.length)
            for(var i in element.invariants) invariants.push(element.invariants[i]);
    });
    return invariants;
}

function getInvariantSet(args, attributes) {
    var urlTemplate = '/citeck/invariants?' + (args.nodeRef ? 'nodeRef=' + args.nodeRef : args.type ? 'type=' + args.type : '') + 
                                              (attributes && attributes.length ? '&attributes=' + attributes.join(',') : '') + 
                                              (args.mode ? '&mode=' + args.mode : '') + 
                                              (args.inlineEdit ? '&inlineEdit=' + args.inlineEdit : '');
    var response = remote.call(urlTemplate);
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

function getWritePermission(nodeRef) {
  if (!nodeRef) return;

  var serviceURI = "/citeck/has-permission?nodeRef=" + nodeRef + "&permission=Write",
      response = eval('(' + remote.call(serviceURI) + ')'); 

  return response;
}

function getViewData(args) {
    var serviceURI = '/citeck/invariants/view?';

    // try-block is used to protect from absense of page object in model.
    // typeof page somehow fails with exception:
    // Invalid JavaScript value of type java.util.HashMap
    try {
        for(var name in page.url.args) {
            if(!name.match(/^param_/)) continue;
            serviceURI += name + '=' + encodeURIComponent(page.url.args[name]) + '&';
        }
    } catch(e) {}

    for(var name in args) {
        if(name == 'htmlid') continue;
        serviceURI += name + '=' + encodeURIComponent(args[name]) + '&';
    }

    var response = remote.call(serviceURI);
    var viewData = eval('(' + response + ')');

    if(response.status == 404) {
        var formUrl = url.context + '/page/components/form?htmlid=' + encodeURIComponent(args.htmlid) + '&submitType=json&showCancelButton=true';
        if(args.type) formUrl += '&itemKind=type&itemId=' + encodeURIComponent(args.type);
        else formUrl += '&itemKind=node&itemId=' + encodeURIComponent(args.nodeRef);
        if(args.viewId) formUrl += '&formId=' + encodeURIComponent(args.viewId);
        if(args.mode) formUrl += '&mode=' + encodeURIComponent(args.mode);
        if(args.param_destination) formUrl += '&destination=' + encodeURIComponent(args.param_destination);

        status.code = 303;
        status.location = formUrl;
        status.redirect = true;
        return null;
    }

    if(response.status != 200) {
        throw 'Can not get view from uri "' + serviceURI + '": ' + response.message;
    }
    
    return viewData;
}