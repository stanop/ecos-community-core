<import resource="classpath:/alfresco/templates/webscripts/ru/citeck/journals2/journals.lib.js">

(function() {
    var journalTypeId = url.templateArgs.journalTypeId;
    if(!journalTypeId) {
        status.setCode(status.STATUS_BAD_REQUEST, "Journal type id should be specified");
        return;
    }

    var journalType = journals.getJournalType(journalTypeId);
    if(!journalType) {
        status.setCode(status.STATUS_NOT_FOUND, "Journal type with id '" + journalTypeId + "' was not found");
        return;
    }

    model.journalType = journalsLib.renderJournalType(journalType);

})();