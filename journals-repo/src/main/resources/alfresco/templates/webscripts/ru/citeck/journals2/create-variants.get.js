function isWritable(variant) {
    try {
        var destinations = variant.assocs["journal:destination"],
            destination = destinations && destinations[0];
        return destination && destination.hasPermission("CreateChildren") ? true : false;
    } catch(e) {
        return false;
    }
}

function getWritableVariants(variants) {
    var writableVariants = [];
    for (var i in variants) {
        if (isWritable(variants[i])) writableVariants.push(variants[i]);
    }
    return writableVariants;
}

(function() {

var siteId = url.templateArgs.site,
    nodetypeId = url.templateArgs.nodetype,
    journalId = url.templateArgs.journal,
    createVariants,
    journal,
    query;

if((!siteId && !nodetypeId && !journalId)) {
    status.setCode(status.STATUS_BAD_REQUEST, "Site or Nodetype or Journal ID should be specified");
    return;
}

var variants = [],
    writable = typeof args.writable != "undefined" && args.writable != null ? args.writable == "true" : null;

// search createVariants by siteId
if (siteId) {
    var site = siteService.getSite(siteId);
    if(site == null) {
        status.setCode(status.STATUS_NOT_FOUND, "Site " + siteId + " not found");
        return;
    }

    var journalsLists = search.query({
        query: 'TYPE:"journal:journalsList" AND @cm\\:name:"site-' + siteId + '-main"',
        language: 'fts-alfresco'
    });

    var journals = [];
    for(var i = 0, ii = journalsLists.length; i < ii; i++) {
        journals = journals.concat(journalsLists[i].assocs["journal:journals"] || []);
    }

    for(var i = 0, ii = journals.length; i < ii; i++) {
        var journalVariants = journals[i].childAssocs["journal:createVariants"] || [];
        for(var j = 0, jj = journalVariants.length; j < jj; j++) {
            var variant = journalVariants[j];
            if(writable == null || writable == isWritable(variant)) {
                variants.push(variant);
            }
        }
    }

    model.siteId = siteId;

} else if (nodetypeId) {

    createVariants = search.query({
        query: 'TYPE:"journal:createVariant" AND @journal\\:type:"' + nodetypeId + '"',
        language: 'fts-alfresco'
    });
    createVariants = createVariants.filter(function (it) {
        return it.properties["journal:type"] == nodetypeId;
    });

    variants = writable ? getWritableVariants(createVariants) : createVariants;

} else {

    query = 'TYPE:"journal:journal" AND @journal\\:journalType:"' + journalId + '"';
    journal = (search.query({ query: query, language: 'fts-alfresco' }) || [])[0];
    createVariants = journal ? journal.childAssocs['journal:createVariants'] || [] : [];

    variants = writable ? getWritableVariants(createVariants) : createVariants;
}

cache.maxAge = 600;

model.writable = writable;
model.createVariants = variants;

})();