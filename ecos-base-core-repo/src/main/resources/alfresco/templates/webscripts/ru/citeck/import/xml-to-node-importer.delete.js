<import resource="classpath:alfresco/module/ecos-base-core-repo/bootstrap/scripts/xml-to-node-parser.js">
<import resource="classpath:alfresco/templates/webscripts/ru/citeck/import/xni-webscripts-utils.js">
/**
 * @author Roman Makarskiy
 */
var descriptions = [];

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

        if (formdata) {
            var statusNode = search.findNode(GLOBAL_STATUS_NODEREF);
            if (!statusNode) {
                var folder = search.selectNodes(XNI_ROOT_PATH)[0];
                generateDescriptionNodeAsync(folder, descriptions);
            }

            for (var i in formdata.fields) {
                var nodeRef = formdata.fields[i].value;
                nodeRefsToExecute.push(nodeRef);

                var processDescription = {};
                processDescription.id = nodeRef;
                processDescription.status = "-";
                descriptions.push(processDescription);
            }

            for (var i2 = 0; i2 < nodeRefsToExecute.length; i2++) {
                var currentNode = search.findNode(nodeRefsToExecute[i2]);
                setStatusAsync(currentNode, STATUS_DELETING);
                updateDescriptionNodeAsync(GLOBAL_STATUS_EXECUTING, descriptions);
                parser.processNodes(METHOD_DELETE, currentNode.nodeRef);
            }

            descriptions = [];
            updateDescriptionNodeAsync(GLOBAL_STATUS_WAIT, descriptions);
        } else {
            status.code = 400;
            status.message = "formData is empty must be specified";
        }

        formdata.cleanup();
        status.code = 200;
        model.success = "true";
    })();

function setStatusAsync(node, status) {
    updateDescription(node.nodeRef, status);
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