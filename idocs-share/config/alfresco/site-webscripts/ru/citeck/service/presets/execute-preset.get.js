(function() {
    var presetId = url.templateArgs.presetId;
    var presetData = {};
    for(var i in args) {
        presetData[i] = args[i];
    }
    sitedata.newPreset(presetId, presetData);
    model.presetId = presetId;
    model.success = true;
})();