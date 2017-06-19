(function()
{
    try {
        var sourceRefs = args['sourceRef'];
        var targetRefs = args['targetRef'];
        var assocType = args['assocTypes'];
        if (assocType === undefined) assocType = "";
        var source = '';
        var target = '';
        var allCreate = 'true';
        if(sourceRefs !== null && targetRefs !== null) {
            var sourceRefsArr = sourceRefs.split(',');
            for (var i = 0; i < sourceRefsArr.length; i++) {
                source = sourceRefsArr[i];
                if (source !== null && source !== '' && source.length !== 0) {
                    var sourceNode = search.findNode(source);
                    if (sourceNode !== null) {
                        var existTargets = [];
                        existTargets = sourceNode.assocs[assocType];
                        var targerRefsArr = targetRefs.split(',');
                        for (var j = 0; j < targerRefsArr.length; j++) {
                            target = targerRefsArr[j];
                            if (target !== null && target !== '' && target.length !== 0 && source !== target) {
                                var targetNode = search.findNode(target);
                                if (targetNode !== null) {
                                    var exist = false;
                                    if (existTargets !== null) {
                                        var tar;
                                        for (var k = 0; k < existTargets.length; k++) {
                                            tar = existTargets[k];
                                            if (tar.nodeRef === target) {
                                                exist = true;
                                            }
                                        }
                                    }
                                    if (!exist) {
                                        sourceNode.createAssociation(targetNode, assocType);
                                    } else {
                                        allCreate = "false";
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        model.data = allCreate;
    } catch (ex) {
        logger.error(ex);
        model.data = "error";
    }

})();