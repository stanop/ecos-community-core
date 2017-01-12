(function() {
    
    var requests = json.get('requests'),
        nodes = {},
        persistedAttributes = {},
        values = [];
    for(var i = 0, ii = requests.length(); i < ii; i++) {
        var request = requests.get(i),
            nodeRef = request.get('nodeRef'),
            node = (nodes[nodeRef] = nodes[nodeRef] || search.findNode(nodeRef)),
            attribute = request.get('attribute');
        
        if(!node) {
            values.push({
                nodeRef: nodeRef,
                attribute: attribute,
                persisted: false
            })
        }
        
        if(!persistedAttributes[nodeRef]) {
            var names = attributes.getPersisted(node),
                persisted = {};
            for(var j in names) {
                persisted[names[j]] = 1;
            }
            persistedAttributes[nodeRef] = persisted;
        }
        
        if(persistedAttributes[nodeRef][attribute]) {
            var value = attributes.get(node, attribute);
            values.push({
                nodeRef: nodeRef,
                attribute: attribute,
                persisted: true,
                value: value
            })
        } else {
            values.push({
                nodeRef: nodeRef,
                attribute: attribute,
                persisted: false
            })
        }
    }
    
    model.values = values;
    
})()