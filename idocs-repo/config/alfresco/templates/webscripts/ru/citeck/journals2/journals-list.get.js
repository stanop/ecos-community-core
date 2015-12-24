(function() {


var journalListName = args.journalsList,
    nodeRef = args.nodeRef,
    query = 'TYPE:"journal:journalsList"';

if (journalListName) {
  query += ' AND @cm\\:name: "' + journalListName + '"';
} else {
  status.setCode(status.STATUS_BAD_REQUEST, "Argument journalsList has not been provided.");
  return;
}

var journalList = search.luceneSearch(query),
    journals, journalByDefault;

if (journalList && journalList.length > 0) {
  if (journalList[0].assocs["journal:default"]) {
    journalByDefault = journalList[0].assocs["journal:default"][0];
  }

  if (journalList[0].assocs["journal:journals"]) {
    journals = journalList[0].assocs["journal:journals"];
  }
}

if (nodeRef) {
  // java services
  var journalService = services.get("journalService"),
      dictionaryService = services.get("dictionaryService");

  var node = search.findNode(nodeRef),
      journalsWithNode = [];

  if (journals) {
    for (j in journals) {
      var journal = journals[j],
          journalType = journalService.getJournalType(journal.properties["journal:journalType"]),
          journalTypeHeaders = journalType.getHeaders();

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
            })

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

for (var j in journals) {
  if (journals[j].equals(journalByDefault)) {
    model.journalByDefault = journalByDefault;
    break;
  }
}

model.journalListId = journalListName;
model.journalsWithNode = journalsWithNode;
model.journalLists = journalList;


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