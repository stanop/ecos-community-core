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
        var root = search.xpathSearch("/app:company_home/st:sites/cm:contracts/cm:dataLists/cm:products-and-services")[0];
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
                                        properties['pas:pricePerUnit'] = targetNode.properties['pas:pricePerUnit'];
                                        properties['pas:type'] = targetNode.properties['pas:type'];
                                        properties['pas:quantity'] = 1;
                                        properties['pas:total'] = 1 * targetNode.properties['pas:pricePerUnit'];
                                        var childNode = root.createNode(null, "pas:pasEntityCopied", properties);
                                        childNode.addAspect("pas:hasUnit");
                                        var unitNode = targetNode.assocs["pas:entityUnit"];
                                        childNode.createAssociation(unitNode[0], "pas:entityUnit");
                                        sourceNode.createAssociation(childNode, assocType);
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