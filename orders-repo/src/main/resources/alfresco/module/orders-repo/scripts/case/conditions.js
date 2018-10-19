<import resource="classpath:alfresco/module/idocs-repo/scripts/case-mt/utils.js">

const CONFIRMATION_STATUS_ACTIVE = "active";
const CONFIRMATION_STATUS_INACTIVE = "inactive";
const OUTCOME_CONFIRM = "Confirmed";
const OUTCOME_CANCEL = "cancel";

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
    var lastCorrectOutcome = document.properties["orders:lastCorrectOutcome"];

    if (currentConfirmationStatus == CONFIRMATION_STATUS_INACTIVE
        && lastConfirmOutcome == OUTCOME_CONFIRM && lastCorrectOutcome != OUTCOME_CANCEL) {
        return true;
    }
    return false;
}

function isConfirmationEndedWithCancel() {
    var currentConfirmationStatus = document.properties["orders:currentConfirmationStatus"];
    var lastCorrectOutcome = document.properties["orders:lastCorrectOutcome"];
    if (currentConfirmationStatus == CONFIRMATION_STATUS_INACTIVE
        && lastCorrectOutcome == OUTCOME_CANCEL) {
        return true;
    }
    return false;
}