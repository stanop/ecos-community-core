(function() {
    
    var defined = {};
    
    if(json.has('types')) {
        var types = json.get('types');
        for(var i = 0, ii = types.length(); i < ii; i++) {
            var typeName = types.get(i);
            defined[typeName] = attributes.getDefined(typeName, false);
        }
    }
    
    if(json.has('nodes')) {
        var nodes = json.get('nodes');
        for(var i = 0, ii = nodes.length(); i < ii; i++) {
            var nodeRef = nodes.get(i);
            var node = search.findNode(nodeRef);
            if(!node) {
                logger.warn("Could not find node " + nodeRef);
                continue;
            }
            defined[nodeRef] = attributes.getDefined(node);
        }
    }
        
    model.attributes = defined;
    
})()