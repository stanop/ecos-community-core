
var attributes = args['attributes'].split(","), data = {};
var dictionaryService = services.get("dictionaryService");
var messageService = services.get("messageService");

for (var i = 0; i < attributes.length; i++) {
    var title = getTitle(attributes[i], dictionaryService, messageService);
    data[attributes[i]] = title || attributes[i];
}

model.attributes = data;

function getTitle(attributeName, dictionaryService, messageService) {
    var attQName = citeckUtils.createQName(attributeName);
    var attribute = dictionaryService.getProperty(attQName);
    if (!attribute) {
        attribute = dictionaryService.getAssociation(attQName);
    }
    return attribute ? attribute.getTitle(messageService) : null;
}