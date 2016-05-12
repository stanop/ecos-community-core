(function()
{
    try {
        var sourceRef = args['sourceRef'];
        var targetRef = args['targetRef'];
        var assocTypes = args['assocTypes'];
        if (assocTypes == undefined) assocTypes = "";
        var sourceR = '';
        var type = '';
        if(sourceRef != null && sourceRef.length != 0 && targetRef != null && targetRef.length != 0) {
            for each (sourceR in sourceRef.split(',')) 
                for each(type in assocTypes.split(',')) {
                    if (type != '') {
                        var sourceNode = search.findNode(sourceR);
                        var targetNode = search.findNode(targetRef);
                        if (sourceNode !== null && targetNode !== null) {
                            sourceNode.removeAssociation(targetNode, type);
                        }
                    }
                    
                }

            model.data = "true";

        }
        model.data = "sourceRef: " + sourceRef + " targetRef: " + targetRef + " assocTypes: " + assocTypes;
    } catch (ex){
        model.data = "Error: " + ex.name + ":" + ex.message;
    }

})();
