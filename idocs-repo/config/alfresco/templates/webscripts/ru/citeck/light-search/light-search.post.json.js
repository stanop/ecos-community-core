(function() {

  function exit(statusCode, statusMsg) {
    status.code = statusCode;
    status.message = statusMsg;
    status.redirect = true;
  }

  function escape(string) {
    return string.replace(/[:]/g, "\\$&");
  }

  function buildAttribute(string) {
    if (string.indexOf(":") != -1) {
      return "@" + escape(string);
    } else {
      return string.toUpperCase();
    }
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

  var object = eval("(" + json.toString() + ")"),
      criteria = object.criteria || [],
      maxItems = 10, skipCount = 0,
      searchQuery = "";

  // criteria-search mode
  if (criteria.length == 0) {
    // build list of criterion
    for (var key in object) {
      if (object[key] && object[key] != "undefined" && object[key] != "null") {
        if (key == "maxItems") maxItems = object[key];
        if (key == "skipCount") skipCount = object[key];

        if (/(field|predicate|value)_\d+/.test(key)) {
          var index = key.split("_")[1],
              name  = key.split("_")[0];
         
          if (!criteria[index]) { criteria[index] = {} };
          if (name == "field") { name = "attribute" };
          criteria[index][name] = object[key];       
        }
      }
    }

    // search TYPE criterion
    for (var i in criteria) {
      if (criteria[i].attribute == "TYPE" && criteria[i].predicate == "type-equals") {
        searchQuery += "TYPE: \"" + criteria[i].value + "\" AND";

        // remove TYPE criterion from list
        criteria.splice(i, 1);
      }
    }
  } else {
    if (object.type) searchQuery += "TYPE: \"" + object.type + "\" AND";
  }

  // light-search mode
  if (searchQuery.indexOf("AND") != -1) searchQuery += " (";
  for (var c in criteria) {
    var criterion = criteria[c];

    if (criterion.attribute && criterion.predicate && criterion.value) {
      if (c != 0) searchQuery += " OR ";
      searchQuery += buildAttribute(criterion.attribute) + ": \"" + applyPredicate(criterion.value, criterion.predicate) + "\"";
    }  
  }
  if (searchQuery.indexOf("AND") != -1) searchQuery += ")";

  var result = search.query({
    query: searchQuery,
    language: "lucene",
    page: { maxItems: maxItems, skipCount: skipCount }
  })

  model.query = searchQuery;
  model.maxItems = maxItems;
  model.skipCount = skipCount;
  model.totalCount = result.length;
  model.result = result;

})();
