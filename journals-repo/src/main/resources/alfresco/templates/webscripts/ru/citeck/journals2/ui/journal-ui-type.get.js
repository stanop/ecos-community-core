(function () {

    var journalsList = (args.journals || "").split(",");
    var result = {};

    for (var i = 0; i < journalsList.length; i++) {
        result[journalsList[i]] = journals.getUIType(journalsList[i]);
    }

    model.result = result;
})();
