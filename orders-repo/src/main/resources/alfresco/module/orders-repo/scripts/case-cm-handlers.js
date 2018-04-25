<import resource="classpath:alfresco/module/idocs-repo/scripts/case-mt/case-cm-confirm-utils.js">

function onCaseCreate() {
}

function onProcessStart() {
}

function beforeConfirm() {

    var lastResults = confirmUtils.getLastConfirmOutcomes(document);

    var optionalPerformers = [];
    for (var performerRef in lastResults) {
        if (lastResults[performerRef] == "Confirm") {
            optionalPerformers.push(search.findNode(performerRef));
        }
    }
    process.optionalPerformers = optionalPerformers;
}

function afterConfirm() {
    confirmUtils.saveConfirmResults(document, process.bpm_package);
}

function isReworkAfterConfirmRequired() {
    if (process.wfcp_performOutcome == 'Reject') {
        return false;
    }

    var decisions = process.bpm_package.childAssocs['wfcp:performResults'] || [];

    for (var i in decisions) {
        var decision = decisions[i];
        if (decision && decision.properties['wfcp:resultOutcome'] == "Rework") {
            return true;
        }
    }
    return false;
}

function isConfirmResultPositive() {
    return !isReworkAfterConfirmRequired() && process.outcome != "Reject";
}

function isSelectSignerRequired() {
    var signer = document.assocs['idocs:signatory'];
    return !signer || signer.length == 0;
}

function changeSigner() {

    var signer = (additionalData.assocs['ctrEvent:signer'] || [])[0];

    if (signer) {
        var docSigner = (document.assocs['idocs:signatory'] || [])[0];
        if (docSigner) {
            document.removeAssociation(docSigner, 'idocs:signatory');
        }
        document.createAssociation(signer, 'idocs:signatory');
    }
}

function resetCase() {

    //reset activities
    caseActivityService.reset(document);

    //remove all events
    var events = document.sourceAssocs['event:document'] || [];
    for each(var event in events) {
        event.addAspect("sys:temporary");
        event.remove();
    }

    //remove all documents
    var docs = document.childAssocs['icase:documents'] || [];
    for each(var doc in docs) {
        doc.addAspect("sys:temporary");
        doc.remove();
    }

    //remove all workflows
    var inactiveWorkflows = services.get('WorkflowService').getWorkflowsForContent(document.nodeRef, false);
    for (var i = 0; i < inactiveWorkflows.size(); i++) {
        services.get('WorkflowService').deleteWorkflow(inactiveWorkflows.get(i).getId());
    }

    var activeWorkflows = services.get('WorkflowService').getWorkflowsForContent(document.nodeRef, false);
    for (var i = 0; i < activeWorkflows.size(); i++) {
        services.get('WorkflowService').deleteWorkflow(activeWorkflows.get(i).getId());
    }

    //reset util properties
    document.properties["orders:currentConfirmationStatus"] = "not-started";
    document.properties["orders:lastConfirmOutcome"] = "";
    document.save();
}

function restartCase() {
    resetCase();
    utils.restartActivity("stage-orders-internal-project");
}
