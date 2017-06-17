/**
 * Created by Maxim Strizhov on 27.09.2016.
 */
var docs = search.query({query:'ASPECT:"icase:case"', language: 'fts-alfresco', store: 'workspace://SpacesStore'});
if (docs) {
    logger.log("Found cases: " + docs.length);
    for(var i = 0; i < docs.length; i++) {
        logger.log(docs[i]);
        if (!docs[i].hasAspect("icase:hasCaseStatus")) {
            logger.log("Processing case " + docs[i]);
            docs[i].addAspect("icase:hasCaseStatus");
        }
    }
} else {
    logger.log("Nothing to process");
}