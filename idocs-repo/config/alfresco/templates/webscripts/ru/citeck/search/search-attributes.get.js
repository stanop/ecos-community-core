var dictionaryService = services.get("DictionaryService");
model.attributes = [];
var attributeName = args.attribute,
    attrFullName = args.attrFull,
    attributeFullName = attrFullName || (attributeName && utils.longQName(attributeName));
var Qnames = dictionaryService.getAllProperties(null);
var iterator = Qnames.iterator();
while (iterator.hasNext()) {
    var propertyQname = iterator.next();
    var property = dictionaryService.getProperty(propertyQname);
    var datatype = property.getDataType().getName().getPrefixString();
    if(!(attributeName || attrFullName) || propertyQname.toString().equals(attributeFullName))
    {
        model.attributes.push({
            fullName: property.getName(),
            datatype: datatype.substring(datatype.search(":") + 1)
        });
    }
}

Qnames = dictionaryService.getAllAssociations();
iterator = Qnames.iterator();
while (iterator.hasNext()) {
    var associationQname = iterator.next();
    var association = dictionaryService.getAssociation(associationQname);
    if(!(attributeName || attrFullName) || associationQname.toString().equals(attributeFullName)) {
        model.attributes.push({
            fullName: association.getName(),
            datatype: "association"
        });
    }
}

var specialAttributes = new XML(config.script);
for(var i in specialAttributes..attribute) {
	var attribute = specialAttributes.attribute[i];
    if(!(attributeName || attrFullName) || attributeFullName == attribute.id || attributeName == attribute.id ) {
		model.attributes.push({
			fullName: "" + attribute.id,
			datatype: "" + attribute.type
		});
	}
}
