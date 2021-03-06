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
                name: element.attribute
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
    var viewParams = ["formKey", "formMode", "formType", "formId"];

    try {
        var urlArgs = page.url.args;

        for (var paramName in urlArgs) {
            var paramValue = urlArgs[paramName];
            if (paramName.match(/^param_/)) {
                params[paramName] = paramValue;
            }
        }
    } catch(e) {}

    for (var paramName in args) {
        var paramValue = args[paramName];
        if (viewParams.indexOf(paramName) >= 0 || paramName.match(/^param_/)) {
            params[paramName] = paramValue;
        }
    }
    if (args["nodeRefAttr"]) {
        params['param_nodeRefAttr'] = args['nodeRefAttr'];
    }

    if (!params.formType) {
        if (args.nodeRef) {
            params.formType = "nodeRef";
            params.formKey = args.nodeRef;
        } else if (args.type) {
            params.formType = "type";
            params.formKey = args.type;
        } else if (args.taskId) {
            params.formType = "taskId";
            params.formKey = args.taskId;
        } else if (args.groupAction) {
            params.formType = "groupAction";
            params.formKey = args.groupAction;
        } else if (args.withoutSavingType) {
            params.formType = "withoutSavingType";
            params.formKey = args.withoutSavingType;
        } else if (args.workflowId){
            params.formType = "workflowId";
            params.formKey = args.workflowId;
        } else {
            throw "Parameters must contain either type, nodeRef or taskId or groupAction or withoutSavingType";
        }
    }

    if (!params.formId && args.viewId) {
        params.formId = args.viewId;
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