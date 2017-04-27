var dictionaryService = services.get("DictionaryService");

model.properties = [];
var propertyQnames = dictionaryService.getAllProperties(null);
var iterator = propertyQnames.iterator()
while(iterator.hasNext())
{
    var props = [];
    var propQName = iterator.next();
    var propDef = dictionaryService.getProperty(propQName);
    var fullQName = propDef.getName();
    var shortQName = propDef.getName().getPrefixString();
    var title = propDef.getTitle();
    props.push({
        fullQName: fullQName,
        shortQName: shortQName,
        title: title
    });
    
    model.properties.push({
        property: propDef,
        props: props
    })
}
