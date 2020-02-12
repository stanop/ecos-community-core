(function () {

    var journalsList = json.getJSONArray('journals');
    var result = {};

    if (journalsList) {
        for (var i = 0; i < journalsList.length(); i++) {
            var journalId = journalsList.get(i);
            result[journalId] = journals.getUIType(journalId);
        }
    }

    model.result = result;
})();
