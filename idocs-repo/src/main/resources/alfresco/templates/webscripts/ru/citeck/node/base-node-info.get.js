
function main() {

    var nodeRef = args['nodeRef'];

    if (nodeRef) {

        var node = search.findNode(nodeRef);

        var updateUtil = services.get("ecos.itemsUpdateState");

        model.result = {
            permissions: {
                Read: node.hasPermission('Read'),
                Write: node.hasPermission('Write')
            },
            modified: node.properties['cm:modified'].getTime(),
            pendingUpdate: updateUtil.isPendingUpdate(node.nodeRef)
        }

    } else {
        throw "nodeRef is a mandatory parameter";
    }
}

main();