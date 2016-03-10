var dictionaryService = services.get("DictionaryService");

var attributes = args.attributes || '';
var atrList = attributes.split(',');
model.metadata = [];
var processedAttributes = {};
for(var i = 0; i < atrList.length; i++)
{
    if (processedAttributes[atrList[i]]) continue;
    processedAttributes[atrList[i]] = true;

    var type = "";
    var dataType = "";
    var name = "";
    var displayLabels = {};
    var atrQName = Packages.org.alfresco.service.namespace.QName.createQName(utils.longQName(atrList[i]));
    var propDef = dictionaryService.getProperty(atrQName);
    var messageService = services.get('messageService');
    var scriptAttributes = services.get('virtualScriptAttributesProvider');

    if(propDef!=null)
    {
        type = "property";
        name = propDef.getTitle();
        dataType = propDef.getDataType().getName().getLocalName();

        var constr = Packages.ru.citeck.ecos.utils.DictionaryUtils.getAllConstraintsForProperty(atrQName, dictionaryService);

        var constraintIter = constr.iterator();
        while (constraintIter.hasNext()) {
            var constraint = constraintIter.next().getConstraint();
            if (constraint instanceof Packages.org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint)
            {
                var listConstraint = constraint;
                var iter = listConstraint.getAllowedValues().listIterator();
                while (iter.hasNext())
                {
                    var evaluator = iter.next();
                    var label = listConstraint.getDisplayLabel(evaluator, messageService);
                    if (label && (!displayLabels[evaluator] || !label.equals(evaluator))) {
                        displayLabels[evaluator] = label;
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
        else if (scriptAttributes.provides(atrQName))
        {
            var attrDef = scriptAttributes.getAttributeDefinition(atrQName);
            type = "property";
            name = attrDef.getTitle();
            dataType = "text";
        }
    }

	if(!type) continue;

    model.metadata.push({
        propName: atrList[i],
        type: type,
        displayName: name,
        datatype: dataType,
        labels: displayLabels
    })
}
