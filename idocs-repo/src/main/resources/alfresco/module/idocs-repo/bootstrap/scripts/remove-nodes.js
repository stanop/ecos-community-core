importPackage(Packages.org.alfresco.service.cmr.repository);

for (var i = 0; i < args.nodes.size(); i++) {
    var node = getNode(args.nodes.get(i));
    if (node) {
        node.remove();
    }
}

function getNode(ref) {
    if (NodeRef.isNodeRef(ref)) {
        return search.findNode(ref);
    }
    return (search.selectNodes(ref) || [])[0];
}