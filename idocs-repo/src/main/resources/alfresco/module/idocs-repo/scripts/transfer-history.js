/** Variables */
var maxItems = 500;
var query = 'TYPE:"idocs:doc"';
var page = {maxItems : maxItems, skipCount : 0};
var sort = [{
    column : "cm:created",
    ascending : true
}];

/** Load first documents */
var result = search.queryResultSet({
        query: query,
        page : page,
        sort : sort
    }
);

var hasMore = result.meta.hasMore;
var nodes = [];
var processedDocuments = 0;

do {
    hasMore = result.meta.hasMore;
    nodes = result.nodes;

    for (var i in nodes) {
        var document = nodes[i];
        historyService.sendAndRemoveOldEventsByDocument(document);
        processedDocuments++;
    }
    print("Processed documents : " + processedDocuments);
    /** Load more results */
    page.skipCount = page.skipCount + maxItems;
    result = search.queryResultSet({
            query: query,
            page : page,
            sort : sort
        }
    );

} while (hasMore);

print("Processed documents : " + processedDocuments);
print("History transfering has been finished");
