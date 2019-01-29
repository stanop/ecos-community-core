var path = "/app:company_home/app:dictionary/cm:cardlets/contracts:agreementCommon-documents-wu-left-c4",
    node = search.selectNodes(path)[0];

if (node) {
    var fixedName = "agreementCommon-documents-wu-left-b7",
        fixedPosition = "b7";

    node.setName(fixedName);
    node.properties["cardlet:regionPosition"] = fixedPosition;
    node.save();
}