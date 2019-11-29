(function() {
    var repositoryHelper = services.get('repositoryHelper'),
        companyhome = search.findNode(repositoryHelper.getCompanyHome()),
        journalPath = '/cm:IDocsRoot/journal:journalMetaRoot/cm:journals/cm:currency-rates',
        countriesJournalNode = (companyhome.childrenByXPath(journalPath) || [])[0],
        countriesJournalCreateVariantNode = (companyhome.childrenByXPath(journalPath + '/journal:default') || [])[0];

    if (countriesJournalNode) {
        utils.setLocale('en_US');
        countriesJournalNode.properties['cm:title'] = 'Currency rates Central Bank';
        countriesJournalNode.save();

        utils.setLocale('ru_RU');
        countriesJournalNode.properties['cm:title'] = 'Курсы валют ЦБ';
        countriesJournalNode.save();
    }

    if (countriesJournalCreateVariantNode) {
        utils.setLocale('en_US');
        countriesJournalCreateVariantNode.properties['cm:title'] = 'Currency rate Central Bank';
        countriesJournalCreateVariantNode.save();

        utils.setLocale('ru_RU');
        countriesJournalCreateVariantNode.properties['cm:title'] = 'Курс валют ЦБ';
        countriesJournalCreateVariantNode.save();
    }
})();