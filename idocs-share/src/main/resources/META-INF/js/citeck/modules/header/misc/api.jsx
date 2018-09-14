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