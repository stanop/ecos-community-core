(function() {
    
    var nodeRef = args.nodeRef,
        type = args.type,
        defined;
    if(!nodeRef && !type) {
        status.setCode(status.STATUS_BAD_REQUEST, "Either nodeRef or type should be specified");
        return;
    } else if(nodeRef && type) {
        status.setCode(status.STATUS_BAD_REQUEST, "Either nodeRef or type should be specified, not both");
        return;
    } else if(nodeRef) {
        var node = search.findNode(nodeRef);
        if(!node) {
            status.setCode(status.STATUS_NOT_FOUND, "Could not find node " + nodeRef);
            return;
        }
        defined = attributes.getDefined(node);
    } else if(type) {
        defined = attributes.getDefined(type);
    }
    
    model.attributes = defined || [];
    
})()