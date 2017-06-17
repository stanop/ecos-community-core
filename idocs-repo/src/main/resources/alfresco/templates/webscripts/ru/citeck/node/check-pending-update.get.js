(function()
{
    var nodeRef = args['nodeRef'];
    var isPendingUpdate = "false";

    if (nodeRef != null && nodeRef.length != 0) {
        var node = search.findNode(nodeRef);
        if (node !== null) {
            var updateUtil = services.get("ecos.itemsUpdateState");
            isPendingUpdate = updateUtil.isPendingUpdate(node.nodeRef).toString();
        }
    }
    model.isPendingUpdate = isPendingUpdate + "";

})()
