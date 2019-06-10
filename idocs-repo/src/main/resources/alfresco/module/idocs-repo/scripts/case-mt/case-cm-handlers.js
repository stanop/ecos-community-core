<import resource="classpath:alfresco/module/idocs-repo/scripts/case-mt/case-cm-confirm-utils.js">

function onCaseCreate() {
}

function onProcessStart() {
    if (document.properties['ecos:documentNumber'] == null || document.properties['ecos:documentNumber'] == ' ') {
        if (document.type == "{http://www.citeck.ru/model/content/ecos/1.0}case") {
            var mapping = {
                'workspace://SpacesStore/cat-doc-kind-application':         'ecos-case-application-num-template',
                'workspace://SpacesStore/cat-doc-kind-claim':               'ecos-case-claim-num-template',
                'workspace://SpacesStore/cat-doc-kind-work-plan':           'ecos-case-work-plan-num-template',
                'workspace://SpacesStore/cat-doc-kind-customer-request':    'ecos-case-customer-req-num-template',
                'workspace://SpacesStore/cat-doc-kind-complaint':           'ecos-case-complaint-num-template'
            };
            var kindNode = document.properties['tk:kind'] ? document.properties['tk:kind'].nodeRef : "";
            if (kindNode) {
                var numberTemplate = search.findNode("workspace://SpacesStore/" + mapping[kindNode]);
            } else {
                var numberTemplate = search.findNode("workspace://SpacesStore/" + "ecos-case-number-template");
            }
        }
        var registrationNumber = enumeration.getNumber(numberTemplate, document);
        document.properties['ecos:documentNumber'] = registrationNumber;
        document.save();
    }
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
    var workflows = services.get('WorkflowService').getWorkflowsForContent(document.nodeRef, false);
    for (var i = 0; i < workflows.size(); i++) {
        services.get('WorkflowService').deleteWorkflow(workflows.get(i).getId());
    }
}
