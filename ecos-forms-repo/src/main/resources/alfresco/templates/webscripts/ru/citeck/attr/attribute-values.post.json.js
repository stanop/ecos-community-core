(function() {
    
    var requests = json.get('requests'),
        nodes = {},
        values = [];

    for(var i = 0, ii = requests.length(); i < ii; i++) {
        var request = requests.get(i),
            nodeRef = request.get('nodeRef'),
            node = (nodes[nodeRef] = nodes[nodeRef] || search.findNode(nodeRef)),
            attribute = request.get('attribute'),
            value = node ? attributes.get(node, attribute) : null;

        if (value == null) {
            values.push({
                nodeRef: nodeRef,
                attribute: attribute,
                persisted: false
            });
        } else {
            values.push({
                nodeRef: nodeRef,
                attribute: attribute,
                persisted: true,
                value: value
            });
        }
    }
    
    model.values = values;
    
})();