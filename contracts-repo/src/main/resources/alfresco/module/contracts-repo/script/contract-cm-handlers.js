<import resource="classpath:alfresco/module/contracts-repo/script/contract-cm-confirm-utils.js">

function onCaseCreate() {

    if (document.properties['contracts:agreementNumber'] == null) {
            if (document.type == "{http://www.citeck.ru/model/contracts/1.0}agreement") {
                var numberTemplate = search.findNode("workspace://SpacesStore/agreement-number-template");
            } else {
                var numberTemplate = search.findNode("workspace://SpacesStore/supAgreement-number-template");
            }
            var registrationNumber = enumeration.getNumber(numberTemplate, document);
            document.properties['contracts:agreementNumber'] = registrationNumber;
    } else {
            var registrationNumber = document.properties['contracts:agreementNumber'];
    }

    //var signingEDS = document.properties['contracts:digiSign'];
    //if (!signingEDS) {
    document.properties['contracts:barcode'] = registrationNumber;
    //}
    document.save();
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

function beforeSigning() {
    return true;
}

function isContractActive() {
    return document.properties['contracts:agreementStartDate'] <= new Date();
}

function cancelRepeal() {
    utils.runAsSystem(function() {
        var startedNow = document.assocs['contracts:repealedActivity'];
        var lastStartedActivities = document.assocs['contracts:lastActiveActivities'];
        logger.log('lastStartedActivities = ' + lastStartedActivities);
        var lastActiveStatus = document.assocs['contracts:lastActiveStatus'] != null ? document.assocs['contracts:lastActiveStatus'][0] : null;
        logger.log('lastActiveStatus = ' + lastActiveStatus);
        if (lastStartedActivities !== undefined && lastStartedActivities !== null) {
            for each (var activity in lastStartedActivities) {
                caseActivityService.startActivity(activity);
                document.removeAssociation(activity, 'contracts:lastActiveActivities');
            }
        } else if (lastActiveStatus !== undefined && lastActiveStatus !== null) {
            var currentStatus = document.assocs['icase:caseStatusAssoc'] != null ? document.assocs['icase:caseStatusAssoc'][0] : null;
            if (currentStatus !== null) {
                document.removeAssociation(currentStatus, 'icase:caseStatusAssoc');
            }
            document.createAssociation(lastActiveStatus, 'icase:caseStatusAssoc');
        }
        if (startedNow !== undefined && startedNow !== null) {
            for each (var started in startedNow) {
                caseActivityService.reset(started);
            }
        }
        document.removeAspect('contracts:repealed');
    });
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
