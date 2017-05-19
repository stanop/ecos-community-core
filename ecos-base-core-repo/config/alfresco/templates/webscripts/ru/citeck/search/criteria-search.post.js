var language = "fts-alfresco";
var data = criteriaSearch.query(requestbody.getContent(), language);

for (var key in data) {
    model[key] = data[key];
}

model.language = language;