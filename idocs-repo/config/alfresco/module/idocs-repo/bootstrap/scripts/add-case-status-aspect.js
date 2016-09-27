/**
 * Created by Maxim Strizhov on 27.09.2016.
 */
var docs = search.luceneSearch('ASPECT:"icase:case"');
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