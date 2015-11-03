<import resource="classpath:/alfresco/extension/scripts/caseDocumentsData.js">

function checkDocumentContains(doc, container) {
    var docs = container.childAssocs["cm:contains"];
    var result = [];
    var rightContainerType = true;
    if(doc.containerType) {
        if(typeof doc.containerType == "string" && (!container.properties['tk:type'] || doc.containerType != container.properties['tk:type'].nodeRef)) {
            rightContainerType = false;
        } else if(typeof doc.containerType == "object" && (!container.properties['tk:type'] || doc.containerType.indexOf(container.properties['tk:type'].nodeRef)) == -1) {
            rightContainerType = false;
        }
    }
    if(doc.loanType) {
        if(typeof doc.loanType == "number" && (!container.assocs['sca:creditType'] || doc.loanType!= container.assocs['sca:creditType'][0].properties["sddl:loanTypeCode"])) {
            rightContainerType = false;
        } else if(typeof doc.loanType == "object" && (!container.assocs['sca:creditType'] || doc.loanType.indexOf(container.assocs['sca:creditType'][0].properties["sddl:loanTypeCode"])) == -1) {
            rightContainerType = false;
        }
    }
    if(!docs && rightContainerType){
        return null;
    }
    if(!docs) {docs = [];}
    if(doc.containerKind == container.typeShort && rightContainerType) {
        for (var i = 0; i < docs.length; i++) {
            if (docs[i].typeShort == "cm:content" || docs[i].typeShort == "sca:creditContract") {
                if (docs[i].properties['tk:kind'] && docs[i].properties['tk:kind'].nodeRef == doc.documentKind) {
                    result.push(docs[i]);
                }
            }
        }
        if(result.length == 0) {
            return null;
        }
    }
    for (var j = 0; j < docs.length; j++) {
        if (doc.containerKind == docs[j].typeShort) {
            var childDocument = checkDocumentContains(doc, docs[j]);
            if (childDocument) {
                result = result.concat(childDocument);
            } else {
                return null;
            }
        }
    }
    return result;
}

function checkAndFinalizeDocuments(documents) {
    var absentDocuments = [];
    var nodesForFinalize = [];
    for (var i = 0; i < documents.length; i++) {
        var type = documents[i].documentKind;
        var nodeForFinalize = checkDocumentContains(documents[i], document);
        if(nodeForFinalize) {
            nodesForFinalize.concat(nodeForFinalize)
        } else {
            absentDocuments.push(getDocumentNameByKindRef(type));
        }
    }
    if (absentDocuments.length == 0) {
        makeFinal(nodesForFinalize);
    } else {
        throw "Не загружены документы: " + absentDocuments.join(", ");
    }
}

function makeFinal(nodes) {
    for (var i = 0; i < nodes.length; i++) {
        constantMaker.makeConstant(nodes[i]);
        nodes[i].setOwner("System");
    }
}

function getMandatoryDocumentsOnStage(stage) {
    var position = stages.indexOf(stage);
    var completedStages = stages.slice(0, position + 1);
    var mandatoryDocuments = [];
    for (var i = 0; i < containerKinds.length; i++) {
        docs = containerKinds[i].documentKinds;
        for (var j = 0; j < docs.length; j++) {
            if (completedStages.indexOf(docs[j].mandatory) != -1) {
                mandatoryDocuments.push({
                    "documentKind": docs[j].nodeRef,
                    "containerKind": containerKinds[i].nodeRef,
                    "containerType": docs[j].containerType ? docs[j].containerType : null,
                    "loanType": docs[j].loanType ? docs[j].loanType : null
                });
            }
        }
    }
    return mandatoryDocuments;
}

function checkAndFinalizeOnStage() {
    var stage = document.properties["idocs:documentStatus"];
    var mandatoryDocumentsOnStage = getMandatoryDocumentsOnStage(stage);
    checkAndFinalizeDocuments(mandatoryDocumentsOnStage);
}

function getDocumentNameByKindRef(kindRef) {
    for (var i = 0; i < documentKinds.length; i++) {
        if (documentKinds[i].nodeRef == kindRef) {
            return documentKinds[i].name;
        }
    }
    return null;
}

checkAndFinalizeOnStage();