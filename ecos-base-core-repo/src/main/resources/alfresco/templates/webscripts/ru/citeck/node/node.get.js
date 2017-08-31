(function () {

    var propsResult = [],
        assocsResult = [],
        childAssocsResult = [],
        childrenResult = [];

    if (args['nodeRef']) {

        var node = search.findNode(args['nodeRef']);

        if (node) {
            model.node = node;
            model.siteShortName = node.siteShortName;

            var propsArr = args['props'] ? extractNames(args['props']) : extractKeys(node.properties),
                assocsArr = args['assocs'] ? extractNames(args['assocs']) : extractKeys(node.assocs),
                childAssocsArr = args['childAssocs'] ? extractNames(args['childAssocs']) : extractKeys(node.childAssocs);

            for (var i = 0; i < propsArr.length; i++) {
                var prop = propsArr[i];
                propsResult.push({
                    key: utils.shortQName(prop),
                    value: node.properties[prop]
                });
            }

            for (var i = 0; i < assocsArr.length; i++) {
                var assoc = assocsArr[i];
                var assocObj = node.assocs[assoc];
                if (assocObj) {
                    var nodeRefs = [];
                    if (assocObj.length > 0) {
                        for (var j = 0; j < assocObj.length; j++)
                            nodeRefs.push(assocObj[j].nodeRef.toString());
                    }
                    assocsResult.push({
                        key: utils.shortQName(assoc),
                        value: nodeRefs
                    });
                }
            }

            for (var i = 0; i < childAssocsArr.length; i++) {
                var assoc = childAssocsArr[i];
                var assocObj = node.childAssocs[assoc];
                if (assocObj) {
                    var nodeRefs = [];
                    if (assocObj.length > 0) {
                        for (var j = 0; j < assocObj.length; j++)
                            nodeRefs.push(assocObj[j].nodeRef.toString());
                    }
                    childAssocsResult.push({
                        key: utils.shortQName(assoc),
                        value: nodeRefs
                    });
                }
            }

            if (args["children"]) {
                var onlyChildren = extractNames(args["children"]),
                    onlyChildrenResult = [];

                for (var c = 0; c < node.children.length; c++) {
                    if (onlyChildren.toString().indexOf(node.children[c].typeShort) >= 0)
                        onlyChildrenResult.push(node.children[c]);
                }

                childrenResult = onlyChildrenResult;
            } else {
                childrenResult = node.children;
            }

        }
    }

    model.children = childrenResult;
    model.props = propsResult;
    model.assocs = assocsResult;
    model.childAssocs = childAssocsResult;

})();


// -----------------
// Private Functions
// ----------------- 

function extractNames(values) {
    var result = [];
    if (values) {
        values = ('' + values).replace(/^\s+|\s+$/g, '');
        var valuesArr = values.split(',');
        for (var i = 0; i < valuesArr.length; i++) {
            var valueName = valuesArr[i].replace(/^\s+|\s+$/g, '');
            if (valueName)
                result.push(valueName);
        }
    }
    return result;
}

function extractKeys(values) {
    var result = [];

    if (values instanceof Array) {
        for (var v = 0; v < values.length; v++) result.push(values[v++].typeShort);
    } else {
        for (var v in values) result.push(v)
    }

    return result;
}