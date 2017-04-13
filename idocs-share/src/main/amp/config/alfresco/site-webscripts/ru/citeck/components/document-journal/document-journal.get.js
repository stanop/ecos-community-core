<import resource="classpath:alfresco/site-webscripts/ru/citeck/components/journals2/journals.lib.js">

(function() {

	model.siteId = args.site || page && page.url && page.url.templateArgs.site;
	model.listId = args.listId;

	var journalsListId = model.journalsListId = getJournalsListId(model.siteId, args.listId),
		nodeRef = model.documentNodeRef = args.nodeRef || page.url.templateArgs.nodeRef || url.templateArgs.nodeRef || page.url.args.nodeRef;
	model.journalsListJSON = journalsListId ? loadJournalsList(journalsListId, nodeRef) : 'null';

	var journalId = model.journalId = getJournalId();
	model.journalJSON = journalId ? loadJournal(journalId) : 'null';

	var filterId = model.filterId = getFilterId();
	model.filterJSON = filterId ? loadFilter(filterId): 'null';

	var settingsId = model.settingsId = getSettingsId();
	model.settingsJSON = settingsId ? loadSettings(settingsId) : 'null';

	var journal = eval('(' + model.journalJSON + ')');

	if(journal) {
		var journalType = model.journalType = journal.type;
		model.filtersListJSON = loadFiltersList(journalType);
		model.settingsListJSON = loadSettingsList(journalType);
	}


  model.outputPredicates = true;
  model.settingsControlMode = args.settingsControlMode;

})();
