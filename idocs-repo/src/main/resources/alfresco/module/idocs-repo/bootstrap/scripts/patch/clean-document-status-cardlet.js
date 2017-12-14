
var results = search.query({
    query: 'TYPE:"cardlet:cardlet" AND =cardlet\\:regionId:"document-status"',
    language: 'fts-alfresco'
});

logger.warn("[clean-document-status-cardlet.js] Found " + results.length + " document-status cardlets");

for (var i in results) {
    results[i].remove();
}