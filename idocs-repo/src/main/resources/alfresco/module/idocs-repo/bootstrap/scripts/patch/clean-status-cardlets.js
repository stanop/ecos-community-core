
cleanCardlets("document-status");
cleanCardlets("case-status");

function cleanCardlets(region) {

    var results = search.query({
        query: 'TYPE:"cardlet:cardlet" AND =cardlet\\:regionId:"' + region + '"',
        language: 'fts-alfresco'
    });

    for (var i in results) {
        logger.warn("[clean-status-cardlets.js] Clean " + region + " cardlet: " + results[i].nodeRef);
        results[i].remove();
    }
}