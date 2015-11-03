(function()
{
    try {
        var sourceRef = args['sourceRef'];
        var targetRef = args['targetRef'];
        var assocTypes = args['assocTypes'];
        if (assocTypes == undefined) assocTypes = "";
        if(sourceRef != null && sourceRef.length != 0 && targetRef != null && targetRef.length != 0) {
            for each(type in assocTypes.split(','))
                if (type != ''){
                    var sourceNode = search.findNode(sourceRef);
                    var targetNode = search.findNode(targetRef);
                    if (sourceNode !== null && targetNode !== null) {
                        sourceNode.removeAssociation(targetNode, type);
                    }
                }
            model.data = "true";
        }
    } catch (ex){
        model.data = "false";
    }

})()
