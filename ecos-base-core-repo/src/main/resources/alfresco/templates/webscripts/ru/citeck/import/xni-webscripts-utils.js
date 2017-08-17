const GLOBAL_STATUS_NODEREF = "workspace://SpacesStore/xni-parser-status";
const XNI_ROOT_PATH = "/app:company_home/app:dictionary/cm:xni-data";
const GLOBAL_STATUS_WAIT = "Wait";
const GLOBAL_STATUS_EXECUTING = "Executing";

function generateDescriptionNodeAsync(node, descriptions) {
    batchExecuter.processArray({
        items: [node],
        batchSize: 1,
        threads: 1,
        onNode: function(row) {
            var properties = [];
            properties['xni:activeParsingDescription'] = transformDescriptionToText(descriptions);
            properties['sys:node-uuid'] = "xni-parser-status";
            properties['xni:prsStatus'] = "Executing";
            row.createNode("xni-parser-status", "xni:parserStatus", properties);
        }
    });
}

function updateDescriptionNodeAsync(status, descriptions) {
    var node = search.findNode(GLOBAL_STATUS_NODEREF);
    batchExecuter.processArray({
        items: [node],
        batchSize: 1,
        threads: 1,
        onNode: function(row) {
            row.properties["xni:prsStatus"] = status;
            row.properties["xni:activeParsingDescription"] = transformDescriptionToText(descriptions);
            row.save();
        }
    });
}

function transformDescriptionToText (descriptions) {
    var descriptionText = "<table><tr><th>nodeRef</th><th>Status</th></tr>";
    for (var i = 0; i < descriptions.length; i ++) {
        descriptionText = descriptionText.concat("<tr><td>").concat(descriptions[i].id).concat('</td><td>')
            .concat(descriptions[i].status).concat("</td></tr>");
    }
    descriptionText = descriptionText.concat('</table>');
    return descriptionText;
}

function arrayObjectsToStringNodeRef(array, type) {
    var newArray = [];
    for (var i = 0; i < array.length; i ++) {
        if (type == "object") {
            newArray.push(array[i] + "");
        } else if (type == "scriptNode") {
            newArray.push(array[i].nodeRef + "");
        }

    }
    return newArray;
}

function sortingOnAssocDependency(array) {
    var stringifyArray = arrayObjectsToStringNodeRef(array, "object");
    var itsSorted = false;
    var iteration = 0;
    var limit = 40;

    while (!itsSorted) {
        var currentIterationIsSorted = true;
        if (iteration >= limit) {
            logger.warn("xml-to-node-importer.post.js: Limit of trying sorting is exhausted. " +
                "When importing data, it is possible not to associate associations.");
            break;
        }
        iteration++;

        for (var i = 0; i < stringifyArray.length; i++) {
            var rootNode = search.findNode(stringifyArray[i]);
            if (rootNode.assocs["xni:dependsOn"] != null && rootNode.assocs["xni:dependsOn"].length > 0) {
                var dependsNodes = rootNode.assocs["xni:dependsOn"];
                dependsNodes = arrayObjectsToStringNodeRef(dependsNodes, "scriptNode");
                for each (var depends in dependsNodes) {
                    var root = stringifyArray[i];
                    if (stringifyArray.indexOf(depends) != -1
                        && stringifyArray.indexOf(depends) > stringifyArray.indexOf(root)) {
                        var dependsElement = stringifyArray[stringifyArray.indexOf(depends)];
                        stringifyArray.splice(stringifyArray.indexOf(depends), 1);
                        stringifyArray.unshift(dependsElement);
                        currentIterationIsSorted = false;
                    }
                }
            }
        }
        itsSorted = currentIterationIsSorted;
    }
    return stringifyArray;
}

function setStatusAsync(node, status) {
    batchExecuter.processArray({
        items: [node],
        batchSize: 1,
        threads: 1,
        onNode: function(row) {
            row.properties["xni:status"] = status;
            row.save();
        }
    });
}

function updateDescription(id, status, descriptions) {
    var  newDescr = descriptions;
    for (var i=0, len=newDescr.length; i<len; i++) {
        if (newDescr[i].id == String(id))  {
            newDescr[i].status = status;
        }
    }
    return newDescr;
}
