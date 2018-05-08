<import resource="classpath:alfresco/module/idocs-repo/scripts/case-mt/utils.js">

const CONFIRMATION_STATUS_ACTIVE = "active";
const CONFIRMATION_STATUS_INACTIVE = "inactive";
const OUTCOME_CONFIRM = "Confirmed";

function shouldRegistrationBeSkipped() {
    return (!!document.properties["orders:skipRegistration"]);
}

function isSignTaskPerformedWithSigned() {
    return utils.isTaskOutcomeEquals("Signed");
}

function isConfirmationStarted() {
    var currentConfirmationStatus = document.properties["orders:currentConfirmationStatus"];
    if (currentConfirmationStatus == CONFIRMATION_STATUS_ACTIVE) {
        return true;
    }
    return false;
}

function isConfirmationEndedWithConfirm() {
    var currentConfirmationStatus = document.properties["orders:currentConfirmationStatus"];
    var lastConfirmOutcome = document.properties["orders:lastConfirmOutcome"];
    if (currentConfirmationStatus == CONFIRMATION_STATUS_INACTIVE
        && lastConfirmOutcome == OUTCOME_CONFIRM) {
        return true;
    }
    return false;
}