var dictionaryService = services.get("DictionaryService");

model.associations = [];
var associationQnames = dictionaryService.getAllAssociations();
logger.log(associationQnames.size());
for(var i = 0; i < associationQnames.size(); i++)
{
    var props = [];
    var assocQName = associationQnames.get(i);
    var assocDef = dictionaryService.getAssociation(assocQName);
    var fullQName = assocDef.getName();
    var shortQName = assocDef.getName().getPrefixString();
    var title = assocDef.getTitle();
    props.push({
        fullQName: fullQName,
        shortQName: shortQName,
        title: title
    });
    
    model.associations.push({
        association: assocDef,
        props: props
    })
}
