(function() {

  var hasPermissionRead = function(node) {
    return node.hasPermission("Read");
  };
  var journalListName = args.journalsList,
      nodeRef = args.nodeRef,
      query = 'TYPE:"journal:journalsList"';

  if (journalListName) {
    query += ' AND @cm\\:name: "' + journalListName + '"';
  } else {
    status.setCode(status.STATUS_BAD_REQUEST, "Argument journalsList has not been provided.");
    return;
  }

  var journalLists = search.luceneSearch(query),
      journalListTitle = "",
      allJournals = [],
      defaultJournals = [];

  for(var i in journalLists) {
    var _journalList = journalLists[i];
    journalListTitle = journalListTitle || _journalList.properties.title;
    var subJournals = _journalList.assocs["journal:journals"];
    if (subJournals && subJournals.length > 0) {
      allJournals = allJournals.concat(subJournals.filter(hasPermissionRead));
    }
    var defaultJournal = _journalList.assocs["journal:default"];
    if (defaultJournal && defaultJournal.length > 0) {
      defaultJournals = defaultJournals.concat(defaultJournal.filter(hasPermissionRead));
    }
  }

  if (nodeRef) {
    // java services
    var journalService = services.get("journalService"),
        dictionaryService = services.get("dictionaryService");

    var node = search.findNode(nodeRef),
        journalsWithNode = [];

    if (allJournals) {
      for (j in allJournals) {
        var journal = allJournals[j],
            journalType = journalService.getJournalType(journal.properties["journal:journalType"]),
            journalTypeHeaders = journalType.getAttributes();

        for (var h = 0; h < journalTypeHeaders.size(); h++) {
          var header = journalTypeHeaders.get(h),
              assoc = dictionaryService.getAssociation(header);

          if (assoc){
            if (node.isSubType(assoc.targetClass.name) || node.hasAspect(assoc.targetClass.name)) {
              var journalCriteria = [],
                  journalJournal = {};

              if (journal.childAssocs["journal:searchCriteria"])
                journalCriteria = journal.childAssocs["journal:searchCriteria"];

              // criterion for search entries by assoc type
              journalCriteria.push({
                properties: {
                  "journal:fieldQName": assoc.name,
                  "journal:predicate": "assoc-contains",
                  "journal:criterionValue": node.nodeRef.toString()
                }
              });

              // create virtual journal if original journal exists on array
              if (journalIsOnArray(journalsWithNode, journal)) {
                journalJournal =  {
                  nodeRef: "virtual-element-" + h + "-" + journal.nodeRef,
                  properties: {
                    "cm:title": journal.properties["cm:title"] + "-" + assoc.title,
                    "journal:journalType": journal.properties["journal:journalType"]
                  }
                }
              } else {
                journalJournal = journal;
              }

              journalsWithNode.push({
                journal: journalJournal,
                criteria: journalCriteria
              });
            }
          }
        }
      }
    }
  }

  model.journalListId = journalListName;
  model.journalListTitle = journalListTitle;
  model.allJournals = allJournals;
  model.journalsWithNode = journalsWithNode;
  model.defaultJournal = defaultJournals[0] || null;


})();


// -----------------
// PRIVATE FUNCTIONS
// -----------------

function journalIsOnArray(array, journal) {
  for (var i in array) {
    if (array[i].journal == journal) return true;
  }
  return false;
}