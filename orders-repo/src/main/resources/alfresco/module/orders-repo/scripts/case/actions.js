<import resource="classpath:alfresco/module/idocs-repo/scripts/case-mt/case-cm-confirm-utils.js">

function onCaseCreate() {
    var ecosConfigService = services.get("ecosConfigService");
    var skipRegValue = ecosConfigService.getParamValue("ordersSkipRegistrationConfig") == "true";
    document.properties["orders:skipRegistration"] = skipRegValue;
    document.save();
    if (!skipRegValue && !document.properties["idocs:registrationNumber"]) {
        var template = search.findNode("workspace://SpacesStore/orders-internal-number-template");
        var registrationNumber = enumeration.getNumber(template, document);
        document.properties['idocs:registrationNumber'] = registrationNumber;
        document.properties['idocs:registrationDate'] = new Date();
        document.save();
    }
}