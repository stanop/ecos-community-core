
function main() {

    var journalId = args.journalId;
    var attribute = args.attribute;

    if (!journalId || !attribute) {
        status.setCode(status.STATUS_BAD_REQUEST, "journalId and attribute should be specified");
        return;
    }

    var journalType = journals.getJournalType(journalId);
    if (!journalType) {
        status.setCode(status.STATUS_NOT_FOUND, "Journal type with id '" + journalId + "' was not found");
        return;
    }

    model.criterion = getFilterCriterion(journalId, attribute);
}

function getFilterCriterion(journalId, attribute) {
    var url = '/api/journals/filter/criterion?journalId=' + journalId + '&attribute=' + attribute;
    var response = remote.call(url);
    return eval('(' + response + ')');
}

main();