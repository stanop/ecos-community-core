importPackage(Packages.org.alfresco.service.cmr.repository);

function main() {

    if (args.nodes) {
        for (var i = 0; i < args.nodes.size(); i++) {
            var nodeData = args.nodes.get(i);
            processNode(nodeData.node, nodeData.attributes);
        }
    } else {
        processNode(args.node, args.attributes);
    }
}

function processNode(node, attrs) {
    var scriptNode = getNode(node);
    for (var key in attrs) {
        attributes.set(scriptNode, key, attrs[key]);
    }
}

function getNode(ref) {
    if (NodeRef.isNodeRef(ref)) {
        return search.findNode(ref);
    }
    return search.selectNodes(ref)[0];
}

main();