<import resource="classpath:/alfresco/templates/webscripts/ru/citeck/journals2/journals.lib.js">

const dictionaryService = services.get("dictionaryService");
const namespaceService = services.get("NamespaceService");

(function() {

    var journalId = args.journalId;
    var attribute = args.attribute;

    if (!journalId || !attribute) {
        status.setCode(status.STATUS_BAD_REQUEST, "journalId and attribute should be specified");
        return;
    }

    var journalType = journals.getJournalType(journalId);
    if (!journalType) {
        status.setCode(status.STATUS_NOT_FOUND, "Journal type with id '" + journalId + "' was not found");
        return;
    }

    var criterion = journalType.getCriteria(attribute);

    model.result = {
        template: journalType.getTemplate() || "default",
        regions: getRegions(criterion)
    };

})();

function getRegions(attribute, criterion) {

    var regions = {
        'actions': { template: 'actions', params: {} },
        'label': { template: 'label', params: {} },
        'predicate': { template: 'predicate', params: {} },
        'input': { template: 'text', params: {} },
        'select': { template: 'select', params: {}}
    };

    var criterionRegions = criterion.getRegions();

    for (var name in criterionRegions) {

        var region = criterionRegions[name];

        var regionData = regions[name] || {template: '', params: {}};
        if (region.getTemplate()) {
            regionData['template'] = region.getTemplate();
        }

        var params = region.getParams();
        var dataParams = regionData['params'];
        for (var key in params) {
            dataParams[key] = params[key];
        }

        regions[name] = regionData;
    }

    return regions;
}

function evalDefaultRegions(regions, attribute, journalTypeParam) {

    if (regions['input'] && regions['input'].template) {
        return;
    }

    if (regions['select'] && regions['select'].template) {
        regions['input'] = { template: 'view', params: {} };
        return;
    }

    var defaultRegions = {
        input: {template: 'text', params: {}},
        select: {template: 'none', params: {}}
    };


    /*
    var propDef = dictionaryService.getProperty(attribute);

    if (propDef != null) {



    } else {
        var assocDef = dictionaryService.getAssociation(attribute);
        if (assocDef != null) {

            var authorityQName = citeckUtils.createQName("cm:authority");
            var targetClass = assocDef.getTargetClass().getName();


            if (dictionaryService.isSubClass(targetClass, authorityQName)) {

                defaultRegions['input'].template = 'view';
                defailtRegions['select'].template = 'orgstruct';

            } else {

                var targetClassName = targetClass.toPrefixString(namespaceService);
                var journalId = getJournalForType(targetClassName);

                if (journalId) {
                    defaultRegions['input'].template = 'view';
                    defaultRegions['select'] = {
                        template: 'journal',
                        prams: {

                        }
                    }
                }
            }


        }
    }*/

}

function getJournalForType(targetClassName) {
    var journalTypes = journals.getAllJournalTypes();
    for(var j in journalTypes) {
        if (journalTypes[j].getOptions()['type'] == targetClassName) {
            return journalTypes[j].id;
        }
    }
    return null;
}

function processSelectRegion(region, classDefinition, attributeType) {

    if (region.template) {
        return;
    }
}