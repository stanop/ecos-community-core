var migrationData = [
    {
        aspect: "cardlet:scoped",
        fields: ['cardlet:allowedType', 'cardlet:allowedAuthorities', 'cardlet:cardMode']
    },
    {
        type: "dms:notificationTemplate",
        fields: ['dms:notificationType', 'dms:workflowName', 'dms:taskName']
    }
];


for each(var mData in migrationData) {

    var query;
    if (mData.aspect) {
        query = 'ASPECT:"' + mData.aspect + '"';
    } else {
        query = 'TYPE:"' + mData.type + '"';
    }
    var nodes = search.query({query: query, language:'fts-alfresco'});
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
    logger.warn("[null-to-empty-migration.js] Type: " + (mData.type || mData.aspect) + " Fixed: " + fixed);
}

