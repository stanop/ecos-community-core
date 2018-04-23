<import resource="classpath:alfresco/module/idocs-repo/scripts/case-mt/utils.js">

function shouldRegistrationBeSkipped() {
    return (!!document.properties["orders:skipRegistration"]);
}

function isSignTaskPerformedWithSigned() {
    return utils.isTaskOutcomeEquals("Signed");
}