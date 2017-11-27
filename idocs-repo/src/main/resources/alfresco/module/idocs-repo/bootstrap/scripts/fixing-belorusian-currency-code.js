(function() {
    var byrCurrencyNode = search.findNode("workspace://SpacesStore/currency-byr");
    if (!byrCurrencyNode) return;
    var currencyCode = String(byrCurrencyNode.properties["idocs:currencyCode"]);
    if (currencyCode === "BYR") {
        byrCurrencyNode.properties["idocs:currencyCode"] = "BYN";
        byrCurrencyNode.save();
    }

})();