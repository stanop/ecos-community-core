(function() {

    var repositoryHelper = services.get('repositoryHelper'),
        companyhome = search.findNode(repositoryHelper.getCompanyHome()),
        journalPath = '/cm:IDocsRoot/journal:journalMetaRoot/cm:journals/cm:ecos-types',
        ecosTypesJournalNode = (companyhome.childrenByXPath(journalPath) || [])[0];

    if (ecosTypesJournalNode) {
        utils.setLocale('en_US');
        ecosTypesJournalNode.properties['cm:title'] = 'Case types';
        ecosTypesJournalNode.save();

        utils.setLocale('ru_RU');
        ecosTypesJournalNode.properties['cm:title'] = 'Типы кейсов';
        ecosTypesJournalNode.save();
    }
})();
