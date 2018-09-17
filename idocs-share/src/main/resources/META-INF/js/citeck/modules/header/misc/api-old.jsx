export function getPhotoSize(userNodeRef) {
    const url = Alfresco.constants.PROXY_URI + "/citeck/node?nodeRef=" + userNodeRef + "&props=ecos:photo";
    const request = new XMLHttpRequest();
    request.open('GET', url, false);
    request.send(null);

    if (request.status === 200 && request.responseText) {
        const data = JSON.parse(request.responseText);
        return data.props["ecos:photo"].size;
    }

    return 0;
}

export function getSitesForUser(username) {
    const url = Alfresco.constants.PROXY_URI + "/api/people/" + encodeURIComponent(username) + "/sites";
    const request = new XMLHttpRequest();
    request.open('GET', url, false);
    request.send(null);

    if (request.status === 200 && request.responseText) {
        return JSON.parse(request.responseText);
    }

    return [];
}

export function buildJournalsListForSite(sitename, journalUrl, req) {
    const url = Alfresco.constants.PROXY_URI + (req ? req : "/api/journals/list?journalsList=site-" + encodeURIComponent(sitename) + "-main");
    let journalsResult = [];

    const request = new XMLHttpRequest();
    request.open('GET', url, false);
    request.send(null);

    if (request.status === 200 && request.responseText) {
        const responseData = JSON.parse(request.responseText);
        const journals = responseData.journals;

        const journalsQuantity = journals.length;
        if (journals && journalsQuantity) {
            for (let j = 0; j < journalsQuantity; j++) {
                let url = (journalUrl ? journalUrl : "/share/page/site/" + sitename + "/journals2/list/main#journal=") + journals[j].nodeRef;
                journalsResult.push({
                    label: journals[j].title,
                    id: "HEADER_" + ((sitename + "_" + journals[j].type).replace(/\-/g, "_")).toUpperCase() + "_JOURNAL",
                    url: url + "&filter=",
                    widgets: buildFiltersForJournal(journals[j].type, url)
                });
            }
        }
    }

    return journalsResult;
}

function buildFiltersForJournal(journalType, filterUrl) {
    const url = Alfresco.constants.PROXY_URI + "/api/journals/filters?journalType=" + journalType;
    let filtersResult = [];

    const request = new XMLHttpRequest();
    request.open('GET', url, false);
    request.send(null);

    if (request.status === 200 && request.responseText) {
        const responseData = JSON.parse(request.responseText);
        const filters = responseData.filters;

        if (filters && filters.length) {
            for (var f = 0; f < filters.length; f++) {
                filtersResult.push({
                    label: filters[f].title,
                    // id: filters[f].type + "-filter",
                    id: "HEADER_" + ((journalType + "_" + filters[f].title).replace(/\-/g, "_")).toUpperCase() + "_FILTER",
                    url: filterUrl + "&filter=" + filters[f].nodeRef
                });
            }
        }
    }

    return filtersResult;
}

export function buildCreateVariantsForSite(sitename, forSlideMenu) {
    const url = Alfresco.constants.PROXY_URI + "/api/journals/create-variants/site/" + encodeURIComponent(sitename);
    let createVariantsPresets = [];

    const request = new XMLHttpRequest();
    request.open('GET', url, false);
    request.send(null);

    if (request.status === 200 && request.responseText) {
        const responseData = JSON.parse(request.responseText);
        const createVariants = responseData.createVariants;

        if (createVariants && createVariants.length > 0) {
            for (let cv = 0; cv < createVariants.length; cv++) {
                createVariantsPresets.push({
                    label: createVariants[cv].title,
                    id: "HEADER_" + ((sitename + "_" + createVariants[cv].type).replace(/\-/g, "_")).toUpperCase(),
                    url: "node-create?type=" + createVariants[cv].type + "&viewId=" + createVariants[cv].formId + "&destination=" + createVariants[cv].destination
                });
            }
        }
    }

    return forSlideMenu ? createVariantsPresets : buildItems(createVariantsPresets, "CREATE_VARIANT");
}

function buildItems(items, groupName) {
    var result = [];

    for (var i = 0; i < items.length; i++) {
        var id = "HEADER_" + (groupName + "_" + items[i].id.replace(/-/, "_")).toUpperCase(),
            configuration = {
                id: id,
                label: items[i].label || "header." + items[i].id + ".label",
                targetUrl: items[i].url,
                targetUrlType: items[i].urlType || "SHARE_PAGE_RELATIVE"
            };

        if (items[i].iconImage) configuration["iconImage"] = items[i].iconImage;
        if (items[i].movable) configuration["movable"] = items[i].movable;

        result.push({
            id: configuration.id,
            name: "alfresco/menus/AlfMenuItem",
            config: configuration
        });
    }

    return result;
}