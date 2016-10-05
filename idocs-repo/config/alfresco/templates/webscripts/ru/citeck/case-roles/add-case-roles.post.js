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
                                        var properties = new Array();
                                        properties['cm:title'] = targetNode.properties['cm:title'];
                                        properties['cm:description'] = targetNode.properties['cm:description'];
                                        properties['icaseRole:varName'] = targetNode.properties['icaseRole:varName'];
                                        var childNode = sourceNode.createNode(null, 'icaseRole:role', properties, "icaseRole:roles");
                                        childNode.addAspect('icaseRole:referenceRole');
                                        childNode.createAssociation(targetNode, "icaseRole:referenceRoleAssoc");
                                        childNode.save();
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
        model.data = "Error: " + ex.name + ":" + ex.message;
    }

})();