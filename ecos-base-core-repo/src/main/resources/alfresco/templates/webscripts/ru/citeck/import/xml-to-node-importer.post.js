<import resource="classpath:alfresco/module/ecos-base-core-repo/bootstrap/scripts/xml-to-node-parser.js">
<import resource="classpath:alfresco/templates/webscripts/ru/citeck/import/xni-webscripts-utils.js">
/**
 * @author Roman Makarskiy
 */
var descriptions = [];
var globalStatus = "";

var updateDescription = function(id, status) {
    var  newDescr = descriptions;
    for (var i=0, len=newDescr.length; i<len; i++) {
        if (newDescr[i].id == String(id))  {
            newDescr[i].status = status;
        }
    }
    descriptions = newDescr;
};

(function () {
        var nodeRefsToExecute = [];
        var countTrying = 0;

        var nextItem = function () {
            nodeRefsToExecute.splice(0, 1);
            countTrying = 0;
            globalStatus = ""
        };

        if (formdata) {
            var statusNode = search.findNode(GLOBAL_STATUS_NODEREF);
            if (!statusNode) {
                var folder = search.selectNodes(XNI_ROOT_PATH)[0];
                generateDescriptionNodeAsync(folder);
            }

            for (var i in formdata.fields) {
                var nodeRef = formdata.fields[i].value;
                nodeRefsToExecute.push(nodeRef);

                var processDescription = {};
                processDescription.id = nodeRef;
                processDescription.status = "-";
                descriptions.push(processDescription);
            }

            nodeRefsToExecute = sortingOnAssocDependency(nodeRefsToExecute);

            for (var i2 = nodeRefsToExecute.length; i2 != 0; i2 = nodeRefsToExecute.length) {

                countTrying++;
                if (countTrying == 20) {
                    nextItem();
                }

                var currentNode = search.findNode(nodeRefsToExecute[0]);
                var status = currentNode.properties["xni:status"];

                logger.error("currentNode:" + currentNode.nodeRef);
                logger.error("status:" + status);

                if (!globalStatus) {
                    globalStatus = status;
                } else {
                    logger.error("globalStatus:" + globalStatus);
                    switch (globalStatus) {
                        case STATUS_NEW:
                            setStatusAsync(currentNode, STATUS_READY);
                            break;
                        case STATUS_READY:
                            setStatusAsync(currentNode, STATUS_IN_PROGRESS);
                            updateDescriptionNodeAsync(GLOBAL_STATUS_EXECUTING);
                            parser.processNodes(METHOD_CREATE, currentNode.nodeRef);
                            break;
                        case STATUS_IN_PROGRESS:
                            break;
                        case STATUS_COMPLETE:
                            updateDescriptionNodeAsync(GLOBAL_STATUS_EXECUTING);
                            nextItem();
                            break;
                        case STATUS_ERROR:
                            updateDescriptionNodeAsync(GLOBAL_STATUS_EXECUTING);
                            nextItem();
                            break;
                        case STATUS_DELETING:
                            break;
                        default:
                            logger.error("DEFAULT: " + globalStatus);
                            setStatusAsync(currentNode, STATUS_ERROR);
                            nextItem();
                            break;
                    }
                }
            }
            descriptions = [];
            logger.error("COMPLETE UPDATE");
            updateDescriptionNodeAsync(GLOBAL_STATUS_WAIT);
        } else {
            status.code = 400;
            status.message = "formData is empty must be specified";
        }

        formdata.cleanup();
        status.code = 200;
        model.success = "true";
    })();

function sortingOnAssocDependency(array) {
    var stringifyArray = arrayObjectsToStringNodeRef(array, "object");
    var itsSorted = false;
    var iteration = 0;
    var limit = 30;

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

function setStatusAsync(node, status) {
    updateDescription(node.nodeRef, status);
    globalStatus = status;
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

function updateDescriptionNodeAsync(status) {
    var node = search.findNode(GLOBAL_STATUS_NODEREF);
    batchExecuter.processArray({
        items: [node],
        batchSize: 1,
        threads: 1,
        onNode: function(row) {
            row.properties["xni:prsStatus"] = status;
            row.properties["xni:activeParsingDescription"] = transformDescriptionToText();
            row.save();
        }
    });
}

function generateDescriptionNodeAsync(node) {
    batchExecuter.processArray({
        items: [node],
        batchSize: 1,
        threads: 1,
        onNode: function(row) {
            var properties = [];
            properties['xni:activeParsingDescription'] = transformDescriptionToText();
            properties['sys:node-uuid'] = "xni-parser-status";
            properties['xni:prsStatus'] = "Executing";
            row.createNode("xni-parser-status", "xni:parserStatus", properties);
        }
    });
}

function transformDescriptionToText () {
    var descriptionText = "<table><tr><th>nodeRef</th><th>Status</th></tr>";
    for (var i = 0; i < descriptions.length; i ++) {
        descriptionText = descriptionText.concat("<tr><td>").concat(descriptions[i].id).concat('</td><td>')
            .concat(descriptions[i].status).concat("</td></tr>");
    }
    descriptionText = descriptionText.concat('</table>');
    return descriptionText;
}