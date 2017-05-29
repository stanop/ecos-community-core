var migrationData = [
    {
        type: "cardlet:cardlet",
        fields: ['cardlet:allowedType', 'cardlet:allowedAuthorities', 'cardlet:cardMode']
    },
    {
        type: "dms:notificationTemplate",
        fields: ['dms:notificationType', 'dms:workflowName', 'dms:taskName']
    }
];


for each(var mData in migrationData) {

    var nodes = search.query({query:'TYPE:"' + mData.type + '"', language:'fts-alfresco'});
    var fixed = 0;

    for each(var n in nodes) {
        for (var j in mData.fields) {
            if (n.properties[mData.fields[j]] == null) {
                n.properties[mData.fields[j]] = "";
                n.save();
                fixed++;
            }
        }
    }
    logger.warn("[null-to-empty-migration.js] Type: " + mData.type + " Fixed: " + fixed);
}

