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
        var root = search.xpathSearch("/app:company_home/st:sites/cm:common-documents/cm:dataLists/cm:products-and-services")[0];
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
                                        properties['pas:pricePerUnit'] = targetNode.properties['pas:pricePerUnit'];
                                        properties['pas:type'] = targetNode.properties['pas:type'];
                                        properties['pas:quantity'] = 1;
                                        properties['pas:total'] = 0;
                                        var childNode = root.createNode(targetNode.properties["cm:name"], "pas:productOrServiceCopied", properties);
                                        //childNode.properties["pas:pricePerUnit"] = targetNode.properties["pas:pricePerUnit"];
                                        //childNode.properties["pas:type"] = targetNode.properties["pas:type"];
                                        childNode.addAspect("pas:hasUnit");
                                        var unitNode = targetNode.assocs["pas:elementUnit"];
                                        childNode.createAssociation(unitNode[0], "pas:elementUnit");
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
        model.data = "error";
    }

})();