<import resource="classpath:alfresco/module/idocs-repo/scripts/case-mt/case-cm-confirm-utils.js">
<import resource="classpath:alfresco/module/idocs-repo/scripts/case-mt/case-cm-handlers.js">

function onCaseCreate() {
    var ecosConfigService = services.get("ecosConfigService");
    var skipRegValue = ecosConfigService.getParamValue("ordersSkipRegistrationConfig") == "true";
    document.properties["orders:skipRegistration"] = skipRegValue;
    document.properties["orders:lastCorrectOutcome"] = "";
    document.save();
    if (!skipRegValue && !document.properties["idocs:registrationNumber"]) {
        var template = search.findNode("workspace://SpacesStore/orders-internal-number-template");
        var registrationNumber = enumeration.getNumber(template, document);
        document.properties['idocs:registrationNumber'] = registrationNumber;
        document.properties['idocs:registrationDate'] = new Date();
        document.save();
    }
}

function beforeRegistration() {

    process['wfrg_autoNumberTemplate'] = "internal-regNumber-template";

    document.setPermission('CreateChildren', 'GROUP_company_accountant');
}

function afterRegistration() {
    document.removePermission('CreateChildren', 'GROUP_company_accountant');
}

function beforeFamiliarization() {

    if (!confirmUtils.canAddPerformer()) {
        return;
    }

    var performers = additionalData.assocs['iEvent:performers'];
    if (!performers.length) throw "Исполнители не выбраны";

    var expandedPerformers = null;
    var authorityUtils = services.get('authorityUtils');

    for (var perfId in performers) {

        var performer = performers[perfId];
        var users = authorityUtils.getContainedUsers(performer.nodeRef, false);
        if (expandedPerformers == null) {
            expandedPerformers = new Packages.java.util.HashSet(users);
        } else {
            expandedPerformers.addAll(users);
        }
    }

    caseRoleService.addAssignees(document, ROLE_PERFORMERS, expandedPerformers);
}
