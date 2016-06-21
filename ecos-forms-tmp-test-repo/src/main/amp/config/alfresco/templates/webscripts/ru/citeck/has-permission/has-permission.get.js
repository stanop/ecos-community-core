(function()
{
    var nodeRef = args['nodeRef'];
    var permission = args['permission'];
    var hasPermission = false;
    if (permission == undefined) permission = "";
    if (nodeRef != null && nodeRef.length != 0) {
        var node = search.findNode(nodeRef);
        if (node !== null) {
            try {
                hasPermission = node.hasPermission(permission);
            } catch(ex){
                throw "Can't check permission " + permission;
            }
        }
    }
    model.data = hasPermission.toString();

})()
