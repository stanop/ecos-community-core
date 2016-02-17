var dictionaryService = services.get("DictionaryService");

var attributes = args.attributes || '';
var atrList = attributes.split(',');
model.metadata = [];
for(var i = 0; i < atrList.length; i++)
{
    var type = "";
    var dataType = "";
    var name = "";
    var displayStr = [];
    var atrQName = Packages.org.alfresco.service.namespace.QName.createQName(utils.longQName(atrList[i]));
    var propDef = dictionaryService.getProperty(atrQName);
    var namespaceService = services.get('messageService');
    if(propDef!=null)
    {
        type = "property";
        name = propDef.getTitle();
        dataType = propDef.getDataType().getName().getLocalName();
        var constr = propDef.getConstraints();
      if(constr)
      {
        for(var j = 0; j < constr.size(); j++)
        {
            var constraint = constr.get(j).getConstraint();
            if (constraint instanceof Packages.org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint)
            {
                var listConstraint = constraint;
                var iter = listConstraint.getAllowedValues().listIterator();
                while (iter.hasNext())
                {
                    var evaluator = iter.next();
                    displayStr.push(evaluator+";"+listConstraint.getDisplayLabel(evaluator, namespaceService));
             
                }
            }
        }
      }
    }
    else
    {
        var assocDef = dictionaryService.getAssociation(atrQName);
        if(assocDef!=null)
        {
            name = assocDef.getTitle();
            dataType = "association";
            if(assocDef.isChild())
            {
                type = "child-association";
            }
            else
            {
                type = "association";
            }
        }
    }

	if(!type) continue;
    model.metadata.push({
        propName: atrList[i],
        type: type,
        displayName: name,
        datatype: dataType,
        labels: displayStr
    })
}