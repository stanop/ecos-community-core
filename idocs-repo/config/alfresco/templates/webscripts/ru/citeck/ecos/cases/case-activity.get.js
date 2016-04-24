(function() {
    
    var node = search.findNode(args.nodeRef);
    if(!node) {
        status.setStatus(status.STATUS_NOT_FOUND, "Can not find node '" + args.nodeRef + "'");
        return;
    }
    
    model.activity = node;
    
})()