<import resource="classpath:alfresco/module/idocs-repo/scripts/case-mt/case-cm-confirm-utils.js">
<import resource="classpath:alfresco/module/idocs-repo/scripts/case-mt/case-cm-handlers.js">

function onCaseCreate() {
    var ecosConfigService = services.get("ecosConfigService");
    var skipRegValue = ecosConfigService.getParamValue("ordersSkipRegistrationConfig") == "true";
    document.properties["orders:skipRegistration"] = skipRegValue;
    document.properties["orders:lastCorrectOutcome"] = "";
    document.save();
    if (skipRegValue && !document.properties["idocs:registrationNumber"]) {
        var template = search.findNode("workspace://SpacesStore/internal-regNumber-template");
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
    if (!performers.length) {
        throw "Исполнители не выбраны";
    }

    var expandedPerformers = new Packages.java.util.HashSet();
    var authorityUtils = services.get('authorityUtils');

    for (var perfIdx in performers) {

        var performer = performers[perfIdx];

        var userName = performer.properties['cm:userName'];
        if (userName) {
            expandedPerformers.add(userName);
        } else {
            var users = authorityUtils.getContainedUsers(performer.nodeRef, false);
            expandedPerformers.addAll(users);
        }
    }

    caseRoleService.addAssignees(document, ROLE_PERFORMERS, expandedPerformers);
}
