function makeId(length) {
    var POSSIBLE_SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    var text = "";
    for (var i = 0; i < length; i++) text += POSSIBLE_SYMBOLS.charAt(Math.floor(Math.random() * POSSIBLE_SYMBOLS.length));

    return text;
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
    var attributeSet = {
        attributes: [],
        sets: [],
        id: "",
        template: view.template,
        params: view.params
    };

    view.elements.forEach(function(element) {
        if (element.type == "field") {
            var attribute = {
                template: element.template,
                name: element.attribute,
                info: {
                    name: element.attribute,
                    type: element.fieldType,
                    nodetype: element.nodetype,
                    datatype: element.datatype,
                    javaclass: element.javaclass
                }
            };
            attributeSet.attributes.push(attribute);
        } else if (element.type == "view") {
            attributeSet.sets.push(getAttributeSet(args, element));
        }
    });

    attributeSet.id = view.params.setId = view.id || makeId(34);

    return attributeSet;
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
        (args.inlineEdit ? '&inlineEdit=' + args.inlineEdit : '')  +
        (args.param_baseRef ? '&baseRef=' + args.param_baseRef : '') +
        (args.param_rootAttributeName ? '&rootAttributeName=' + args.param_rootAttributeName : '');
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

function _convertNodeViewGetParams(args) {

    var params = {};
    var paramsList = ["formKey", "formMode", "formType"];
    for (var idx in paramsList) {
        var key = paramsList[idx];
        if (args[key]) {
            params[key] = args[key];
        }
    }

    if (!params.formType) {
        if (args.nodeRef) {
            params.formType = "nodeRef";
            params.formKey = args.nodeRef;
        } else if (args.type) {
            params.formType = "type";
            params.formKey = args.type;
        } else {
            throw "Parameters must contain either type or nodeRef";
        }
    }

    if (!params.formMode && args.mode) {
        params.formMode = args.mode;
    }
    return params;
}

function getViewData(args) {

    var serviceURI = '/citeck/ecos/forms/node-view?';

    var params = _convertNodeViewGetParams(args);
    for (var name in params) {
        serviceURI += name + '=' + encodeURIComponent(params[name]) + '&';
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