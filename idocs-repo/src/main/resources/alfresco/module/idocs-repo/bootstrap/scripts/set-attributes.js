importPackage(Packages.org.alfresco.service.cmr.repository);

function main() {
    var node = getNode(args.node);
    for (var key in args.attributes) {
        attributes.set(node, key, args.attributes[key]);
    }
}

function getNode(ref) {
    if (NodeRef.isNodeRef(ref)) {
        return search.findNode(ref);
    }
    return search.selectNodes(ref)[0];
}

main();