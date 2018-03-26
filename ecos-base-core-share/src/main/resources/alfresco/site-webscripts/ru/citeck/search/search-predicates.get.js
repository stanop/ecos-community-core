
function main() {
    model.result = {
        datatype: args.datatype,
        predicates: getPredicates(args.datatype)
    };

    cache.maxAge = 3600;
}

function getPredicates(datatype) {

    if (!datatype) {
        return [];
    }

    var listsRoot = config.scoped["Search"]["predicate-lists"];
    var emptyList = {size: function() {return 0;}};

    if (listsRoot) {

        var lists = listsRoot.childrenMap["predicate-list"] || emptyList;

        for (var listIdx = 0; listIdx < lists.size(); listIdx++) {

            var list = lists.get(listIdx);
            var types = list.childrenMap["type"] || emptyList;

            for (var typeIdx = 0; typeIdx < types.size(); typeIdx++) {

                if (types.get(typeIdx).value == datatype) {

                    var predicates = list.childrenMap["predicate"] || emptyList;
                    var result = [];

                    for (var predIdx = 0; predIdx < predicates.size(); predIdx++) {
                        var pred = predicates.get(predIdx);
                        result.push({
                            "id": pred.value,
                            "label": msg.get(pred.attributes["label"] || "predicate." + pred.value),
                            "needsValue": pred.attributes["needsValue"] != "false"
                        });
                    }
                    return result;
                }
            }
        }
    }

    return [];
}

main();