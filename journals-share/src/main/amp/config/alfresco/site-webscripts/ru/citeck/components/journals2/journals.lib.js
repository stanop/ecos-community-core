function getJournalsListId(site, listId) {
	if(args.journalsListId) {
		return args.journalsListId;
	}
	if(typeof listId == "undefined" || listId == null || listId == "") {
		return null;
	}
	if(site) {
		return "site-" + site + "-" + listId;
	} else {
		return "global-" + listId;
	}
}

function getJournalId() {
	return args.journalId || null;
}

function getFilterId() {
	return args.filterId || null;
}

function getSettingsId() {
	return args.settingsId || null;
}

function loadObject(url, urlArgs) {
	var finalUrl = url + "?";
	if(urlArgs) {
	    for(var name in urlArgs) {
	        finalUrl += name + "=" + encodeURIComponent(urlArgs[name]) + "&";
	    }
	}
	var result = remote.call(finalUrl);
	if(result.status == 200) {
		return result + "";
	} else {
		return "null";
	}
}

function loadJournalsLists() {
	return loadObject("/api/journals/lists");
}

function loadJournalsList(journalsListId, nodeRef) {
	var requestURIObject = { journalsList: journalsListId };
	if (nodeRef) requestURIObject["nodeRef"] = nodeRef;
	return loadObject("/api/journals/list", requestURIObject);
}

function loadJournal(journalId) {
	return loadObject("/api/journals/journals-config", { nodeRef: journalId });
}

function loadFilter(filterId) {
	return loadObject("/api/journals/filter", { nodeRef: filterId });
}

function loadFiltersList(journalType) {
	var json = loadObject("/api/journals/filters", { journalType: journalType });
	var model = eval('(' + json + ')');
	return jsonUtils.toJSONString(model.filters);
}

function loadSettings(settingsId) {
	return loadObject("/api/journals/selected-attributes", { nodeRef: settingsId });
}

function loadSettingsList(journalType) {
	var json = loadObject("/api/journals/settings", { journalType: journalType });
	var model = eval('(' + json + ')');
	return jsonUtils.toJSONString(model ? (model.settings ? model.settings : {}) : {});
}

function fillModel() {

	model.siteId = args.site || page && page.url && page.url.templateArgs.site;
	model.listId = args.listId;

	var journalsListId = model.journalsListId = getJournalsListId(model.siteId, args.listId);
	model.journalsListJSON = journalsListId ? loadJournalsList(journalsListId) : 'null';

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

	fillActions();
}

function fillActions() {
	var multiActions = getScopedConfig("DocumentLibrary", "multi-select", "action", function(a) {return a.attributes;});
	var hiddenActions = getScopedConfig("Journals", "hidden-actions", "action", function(a) {return a.attributes.id;});

	var actions = [];

	for (var i = 0; i < multiActions.length; i++) {
		var action = multiActions[i];
		if (hiddenActions.indexOf(action["id"]) == -1) {
			actions.push(action);
		}
	}

	model.multiActions = actions;
}

function getScopedConfig(scope, section, itemType, mapFunc) {
	var scopeConfig = config.scoped[scope];
	if (!scopeConfig) {
		return [];
	}
	var sectionConfig = scopeConfig[section];
	if (!sectionConfig) {
		return [];
	}
	var itemsCollection = sectionConfig.childrenMap[itemType];
	if (!itemsCollection) {
		return [];
	}
	var result = [];
	for (var i = 0; i < itemsCollection.size(); i++) {
		var item = itemsCollection.get(i);
		result.push(mapFunc? mapFunc(item) : item);
	}
	return result;
}