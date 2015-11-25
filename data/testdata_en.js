(function(models) {
    var root = search.xpathSearch("/cm:IDocsRoot/journal:journalMetaRoot/cm:journals/cm:original-location/journal:default")[0].assocs["journal:destination"][0];
    for(var i in models) {
        root.createNode(null, "contracts:originalLocation", {
            "cm:name": models[i].name,
        });
    }
})([
    { name: "Printed" },
    { name: "Sent to contractor" },
    { name: "Recieved from contractor" },
    { name: "In archive" },
]);
