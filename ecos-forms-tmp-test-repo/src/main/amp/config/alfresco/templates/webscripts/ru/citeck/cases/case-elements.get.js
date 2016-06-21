(function() {
    
    var elementType = args.elementType;
    if(!elementType) {
        status.setCode(status.STATUS_BAD_REQUEST, "Argument elementType is missing");
        return;
    }
    
    var caseNode = search.findNode(args.nodeRef);
    if(!caseNode) {
        status.setCode(status.STATUS_NOT_FOUND, "Can not find node by nodeRef '" + args.nodeRef + "'");
        return;
    }
    
    if(!caseNode.hasAspect("icase:case")) {
        status.setCode(status.STATUS_BAD_REQUEST, "Specified node is not a case");
        return;
    }
    
    model.elements = caseService.getElements(caseNode, elementType);
    
})()