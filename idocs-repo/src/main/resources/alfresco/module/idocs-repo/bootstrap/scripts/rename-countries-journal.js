(function() {
    var repositoryHelper = services.get('repositoryHelper'),
        companyhome = search.findNode(repositoryHelper.getCompanyHome()),
        journalPath = '/cm:IDocsRoot/journal:journalMetaRoot/cm:journals/cm:country-iso3166',
        countriesJournalNode = (companyhome.childrenByXPath(journalPath) || [])[0],
        countriesJournalCreateVariantNode = (companyhome.childrenByXPath(journalPath + '/journal:default') || [])[0];

    if (countriesJournalNode) {
        utils.setLocale('en_US');
        countriesJournalNode.properties['cm:title'] = 'Countries';
        countriesJournalNode.save();

        utils.setLocale('ru_RU');
        countriesJournalNode.properties['cm:title'] = 'Страны';
        countriesJournalNode.save();
    }

    if (countriesJournalCreateVariantNode) {
        utils.setLocale('en_US');
        countriesJournalCreateVariantNode.properties['cm:title'] = 'Country';
        countriesJournalCreateVariantNode.save();

        utils.setLocale('ru_RU');
        countriesJournalCreateVariantNode.properties['cm:title'] = 'Страна';
        countriesJournalCreateVariantNode.save();
    }
})();