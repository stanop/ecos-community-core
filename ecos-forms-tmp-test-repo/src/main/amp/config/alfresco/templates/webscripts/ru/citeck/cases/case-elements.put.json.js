(function() {

    // checks input parameters
    if(!json.has('elements')) {
        status.setCode(status.STATUS_BAD_REQUEST, "Argument 'elements' is not specified");
        return;
    }
    if(!json.has('caseNode')) {
        status.setCode(status.STATUS_BAD_REQUEST, "Case node is not specified");
        return;
    }
    if(!json.has('elementType')) {
        status.setCode(status.STATUS_BAD_REQUEST, "Config name is not specified");
        return;
    }
    
    var elementNodeRefs = json.get('elements');
    var caseNodeRef = json.get('caseNode');
    var elementType = json.get('elementType');
    
    var caseNode = search.findNode(caseNodeRef);
    if(!caseNode) {
        status.setCode(status.STATUS_NOT_FOUND, "Can not find node by nodeRef: " + caseNodeRef);
        return;
    }

    for(var i = 0, ii = elementNodeRefs.length(); i < ii; i++) {
        var element = search.findNode(elementNodeRefs.get(i));
        if(!element) continue;
        caseService.addElement(element, caseNode, elementType);
    }
    
    model.data = "true";

})();
