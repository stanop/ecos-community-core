(function() {
    
    var nodeRefs = json.get('nodeRefs');
    var nodes = [];
    for(var i = 0, ii = nodeRefs.length(); i < ii; i++) {
        var node = search.findNode(nodeRefs.get(i));
        if(node != null) {
            nodes.push(node);
        }
    }
    
    model.nodeRefs = nodeRefs;
    model.nodes = nodes;
})()

