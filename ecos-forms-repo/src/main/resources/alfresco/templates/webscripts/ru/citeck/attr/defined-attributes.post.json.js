(function() {
    
    var defined = {};
    
    if(json.has('types')) {
        var types = json.get('types');
        for(var i = 0, ii = types.length(); i < ii; i++) {
            var typeName = types.get(i);
            defined[typeName] = filter(attributes.getDefined(typeName, false), function(attr) {
                return attr.indexOf("_added") == -1;
            });
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
            defined[nodeRef] = filter(attributes.getDefined(node), function(attr) {
                return attr.indexOf("_added") == -1;
            });
        }
    }

    model.attributes = defined;
    
})()

function filter(array, callback) {
    var result = [];
    for (var i = 0; i < array.length; i++) {
        if (callback(array[i], i)) result.push(array[i]);
    }
    return result;
}