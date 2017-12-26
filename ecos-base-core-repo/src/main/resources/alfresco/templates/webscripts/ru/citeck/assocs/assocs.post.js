(function () {
    try {
        var json = requestbody.getContent(),
            jsonData = jsonUtils.toObject(json),
            sourceRefs = (json && jsonData && jsonData.sourceRef) ? jsonData.sourceRef : args['sourceRef'],
            targetRefs = (json && jsonData && jsonData.targetRef) ? jsonData.targetRef : args['targetRef'],
            assocType = (json && jsonData && jsonData.assocTypes) ? jsonData.assocTypes : args['assocTypes'];

        if (assocType === undefined) assocType = "";

        var source = '',
            target = '',
            allCreate = 'true';

        if (sourceRefs !== null && targetRefs !== null) {
            var sourceRefsArr = sourceRefs.split(',');

            for (var i = 0; i < sourceRefsArr.length; i++) {
                source = sourceRefsArr[i];

                if (source !== null && source !== '' && source.length !== 0) {
                    var sourceNode = search.findNode(source);

                    if (sourceNode !== null) {
                        var existTargets = sourceNode.assocs[assocType],
                            targetRefsArr = targetRefs.split(',');

                        for (var j = 0; j < targetRefsArr.length; j++) {
                            target = targetRefsArr[j];

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
        model.data = '\"error\"';
    }

})();