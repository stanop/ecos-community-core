(function() {

    // checks input parameters
    if(!args.nodeRef) {
        status.setCode(status.STATUS_BAD_REQUEST, "Argument 'nodeRef' is not specified");
        return;
    }
    if(!args.caseNode) {
        status.setCode(status.STATUS_BAD_REQUEST, "Case node is not specified");
        return;
    }
    if(!args.elementType) {
        status.setCode(status.STATUS_BAD_REQUEST, "Config name is not specified");
        return;
    }
    
    var elementNodeRef = args.nodeRef;
    var caseNodeRef = args.caseNode;
    var elementType = args.elementType;
    
    var caseNode = search.findNode(caseNodeRef);
    if(!caseNode) {
        status.setCode(status.STATUS_NOT_FOUND, "Can not find node by nodeRef: " + caseNodeRef);
        return;
    }

    var elementNode = search.findNode(elementNodeRef);
    if(!elementNode) {
        status.setCode(status.STATUS_NOT_FOUND, "Can not find node by nodeRef: " + elementNode);
        return;
    }

    caseService.removeElement(elementNode, caseNode, elementType);
    
    model.data = "true";

})();
