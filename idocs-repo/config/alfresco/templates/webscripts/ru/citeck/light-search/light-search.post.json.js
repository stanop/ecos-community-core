(function() {

  function exit(statusCode, statusMsg) {
    status.code = statusCode;
    status.message = statusMsg;
    status.redirect = true;
  }

  function escape(string) {
    return string.replace(/[:]/g, "\\$&");
  }

  function applyPredicate(value, predicate) {
    switch (predicate) {
      case "string-contains":
        return "*" + value + "*";
      case "string-ends-with":
        return "*" + value;
      case "string-starts-with":
        return value + "*";
      case "string-equals":
        return value;

      // case "string-not-equals":
      // case "string-empty":
      // case "string-not-empty":
    }
  }

  if (!json.has("criteria")) {
    exit(status.STATUS_BAD_REQUEST, "Argument 'criteria' should be specified")
    return;
  }

  if (!json.has("type")) {
    exit(status.STATUS_BAD_REQUEST, "Argument 'type' should be specified")
    return;
  }

  var object = eval("(" + json.toString() + ")"),
      criteria = object.criteria,
      maxItems = object.maxItems || 10,
      skipCount = object.skipCount || 0,
      searchQuery = "TYPE: \"" + object.type + "\" AND";

  searchQuery += " (";
  for (var c = 0; c < criteria.length; c++) {
    var criterion = criteria[c];

    if (criterion.attribute && criterion.predicate && criterion.value) {
      var attribute = (criterion.attribute.indexOf(":") != -1 ? "@" : "") + escape(criterion.attribute);
      searchQuery += attribute + ": \"" + applyPredicate(criterion.value, criterion.predicate) + "\"";

      if (c != criteria.length - 1) searchQuery += " OR ";
    }  
  }
  searchQuery += ")";

  var result = search.query({
    query: searchQuery,
    language: "lucene",
    page: {
      maxItems: maxItems,
      skipCount: skipCount
    }
  })

  model.query = searchQuery;
  model.maxItems = maxItems;
  model.skipCount = skipCount;
  model.totalCount = result.length;
  model.result = result;

})();
