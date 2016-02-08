(function() {

    // getting arguments
    var title = json.get('title'),
        journalTypes = json.getJSONArray('journalTypes'),
        visibleAttributes = json.getJSONArray('visibleAttributes');

    if (!title || title == null) {
        title = "";
    }

    // checking for arguments definitions
    if (!journalTypes) {
        status.setCode(status.STATUS_BAD_REQUEST, "Argument 'journalTypes' is required");
        return;
    }
    if (!visibleAttributes) {
        status.setCode(status.STATUS_BAD_REQUEST, "Argument 'visibleAttributes' is required");
        return;
    }

    // preparing arrays
    var types = [];
    for(var i = 0, ii = journalTypes.length(); i < ii; i++) {
        types.push(journalTypes.get(i));
    }
    var attributes = [];
    for(var i = 0, ii = visibleAttributes.length(); i < ii; i++) {
        attributes.push(visibleAttributes.get(i));
    }

    // create the node for new settings item
    var settingsItem = userhome.createNode(null, "journal:settings", {
        "cm:title": title,
        "journal:journalTypes": types,
        "journal:visibleAttributes": attributes
    });

    model.settingsItem = settingsItem;

})();
