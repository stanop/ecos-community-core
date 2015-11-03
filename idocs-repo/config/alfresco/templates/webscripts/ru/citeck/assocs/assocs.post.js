(function()
{
    try {
        var sourceRefs = args['sourceRef'];
        var targetRefs = args['targetRef'];
        var assocType = args['assocTypes'];
        if (assocType == undefined) assocType = "";
        var source = '';
        var target = '';
        var allCreate = 'true';
        if(sourceRefs != null && targetRefs != null) {
            for each(source in sourceRefs.split(','))
                if (source != '' && source.length != 0 ){
                    var sourceNode = search.findNode(source);
                    if (sourceNode !== null) {
                        var existTargets = [];
                        existTargets = sourceNode.assocs[assocType];
                        for each(target in targetRefs.split(','))
                            if (target != '' && target.length != 0 && source != target){
                                var targetNode = search.findNode(target);
                                if (targetNode !== null) {
                                    var exist = false;
                                    for each (tar in existTargets)
                                        if(tar.nodeRef == target) {
                                            exist = true;
                                        }
                                    if(!exist) {
                                        sourceNode.createAssociation(targetNode, assocType);
                                    } else {
                                        allCreate = "false";
                                    }
                                }
                            }
                    }
                }
            model.data = allCreate;
        }
    } catch (ex){
        model.data = "error";
    }

})()