var dictionaryService = services.get("DictionaryService");

var attributes = args.attributes || '',
    atrList = attributes.split(','),
    processedAttributes = {};

model.metadata = [];

for(var i = 0; i < atrList.length; i++) {
    if (processedAttributes[atrList[i]]) continue;
    processedAttributes[atrList[i]] = true;

    var type = "",
        dataType = "",
        name = "",
        displayLabels = {},
        attrName = atrList[i],
        atrQName = Packages.org.alfresco.service.namespace.QName.createQName(utils.longQName(attrName)),
        nodetype = "",
        journalTypeId = "",
        propDef = dictionaryService.getProperty(atrQName),
        messageService = services.get('messageService'),
        scriptAttributes = services.get('virtualScriptAttributesProvider');

    if(propDef != null) {
        type = "property";
        name = propDef.getTitle();
        dataType = propDef.getDataType().getName().getLocalName();

        var constr = Packages.ru.citeck.ecos.utils.DictionaryUtils.getAllConstraintsForProperty(atrQName, dictionaryService);

        var constraintIter = constr.iterator();
        while (constraintIter.hasNext()) {
            var constraint = constraintIter.next().getConstraint();
            if (constraint instanceof Packages.org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint) {
                var listConstraint = constraint,
                    iter = listConstraint.getAllowedValues().listIterator();
                while (iter.hasNext()) {
                    var evaluator = iter.next(),
                        label = listConstraint.getDisplayLabel(evaluator, messageService);
                    if (label && (!displayLabels[evaluator] || !label.equals(evaluator))) {
                        displayLabels[evaluator] = label;
                    }
                }
            }
        }
    } else {
        var assocDef = dictionaryService.getAssociation(atrQName);
        if(assocDef != null) {
            var journalTypes = journals.getAllJournalTypes();

            nodetype = assocDef.getTargetClass().getName();
            name = assocDef.getTitle();
            dataType = "association";
            type = assocDef.isChild() ? "child-association" : "association";

            for(var j in journalTypes) {
                var journalType = journals.getJournalType(journalTypes[j].id);
                if (journalType.getOptions()["type"] == nodetype.getPrefixString()) {
                    journalTypeId = journalTypes[j].id;
                    break;
                }
            }
        } else if (scriptAttributes.provides(atrQName)) {
            var attrDef = scriptAttributes.getAttributeDefinition(atrQName);
            type = "property";
            name = attrDef.getTitle();
            dataType = "text";
        } else {
            type = "property";
            name = attrName;
            dataType = "text";
        }
    }

    if(!type) continue;

    model.metadata.push({
        propName: atrList[i],
        type: type,
        displayName: name,
        datatype: dataType,
        labels: displayLabels,
        nodetype: nodetype,
        journalType: journalTypeId
    })
}
