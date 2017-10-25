var language = "fts-alfresco";
var data = criteriaSearch.query(requestbody.getContent(), language);

for (var key in data) {
    model[key] = data[key];
}

model.language = language;

var globalProps = services.get("global-properties");
model.personFirstName = globalProps["reportProducer.personFirstName"];
model.personLastName = globalProps["reportProducer.personLastName"];
model.personMiddleName = globalProps["reportProducer.personMiddleName"];