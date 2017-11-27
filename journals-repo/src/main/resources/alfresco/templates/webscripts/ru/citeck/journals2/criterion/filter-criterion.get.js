importPackage(Packages.org.alfresco.service.cmr.dictionary);
importPackage(Packages.org.alfresco.repo.dictionary.constraint);
importPackage(Packages.org.alfresco.model);

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

    var criterion = journalType.getCriterion(attribute);

    model.result = {
        template: criterion.getTemplate() || "default",
        regions: getRegions(criterion, attribute, journalType.getOptions()['type'])
    };

})();

function getRegions(criterion, attribute, journalTypeParam) {

    var regions = {
        'actions': { template: 'actions', params: {} },
        'label': { template: 'label', params: {} },
        'predicate': { template: 'predicate', params: {} },
        'input': { template: '', params: {} },
        'select': { template: '', params: {}}
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

    evalDefaultRegions(regions, attribute, journalTypeParam);

    return regions;
}

function evalDefaultRegions(regions, attribute, journalTypeParam) {

    if (regions['input'].template) {
        return;
    }

    if (regions['select'].template) {
        regions['input'].template = 'view';
        return;
    }

    var attrQName = citeckUtils.createQName(attribute);
    var typeQName = journalTypeParam ? citeckUtils.createQName(journalTypeParam) : null;

    var assocDef = dictionaryService.getAssociation(attrQName);

    if (assocDef) {

        var nodetype = assocDef.getTargetClass().getName();
        var typeShortName = nodetype.toPrefixString(namespaceService);

        if (dictionaryService.isSubClass(nodetype, ContentModel.TYPE_AUTHORITY)) {

            var authorityTypes = [];
            if (dictionaryService.isSubClass(ContentModel.TYPE_PERSON, nodetype)) {
                authorityTypes.push('USER');
            }
            if (dictionaryService.isSubClass(ContentModel.TYPE_AUTHORITY_CONTAINER, nodetype)) {
                authorityTypes.push('GROUP');
            }

            regions['input'].template = 'view';
            regions['select'].template = 'orgstruct';
            regions['select'].params['allowedAuthorityType'] = authorityTypes.join(',');

        } else {

            var journalId = getJournalByType(typeShortName);
            if (journalId) {
                regions['input'].template = 'view';
                regions['select'].template = 'select-journal';
                regions['select'].params['journalType'] = journalId;
            }
        }

    } else {

        var propDef = services.get('dictUtils').getPropDef(typeQName, attrQName);

        if (propDef) {

            regions['select'].template = 'none';

            var constraints = propDef.getConstraints();
            for (var i = 0; i < constraints.size(); i++) {
                if (constraints.get(i).getConstraint() instanceof ListOfValuesConstraint) {
                    regions['input'].template = "select";
                    break;
                }
            }

            if (!regions['input'].template) {

                var propType = propDef.getDataType().getName().getLocalName();

                var mapping = {
                    'int': 'number',
                    'long': 'number',
                    'float': 'number',
                    'double': 'number',
                    'date': 'date',
                    'datetime': 'datetime',
                    'boolean': 'boolean',
                    'category': 'select'
                };
                if (mapping[propType]) {
                    regions['input'].template = mapping[propType];
                }
            }
        }
    }

    if (!regions['input'].template) regions['input'].template = 'text';
    if (!regions['select'].template) regions['select'].template = 'none';
}

function getJournalByType(typeShortName) {

    var journalTypes = journals.getAllJournalTypes();

    for(var j in journalTypes) {
        var journalType = journalTypes[j];
        if (journalType.getOptions()["type"] == typeShortName) {
            return journalTypeId = journalTypes[j].id;
        }
    }
    return null;
}
