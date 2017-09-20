function getContainerKind(container) {
    return container;
}

function processContainerKind(model, containerKind, currentLevels) {
    if(model.containerKinds[containerKind.nodeRef]) return;
    
    var kinds = [],
        index = {},
        levels = completeness.getAllLevels(containerKind),
        childLevels = {};

    for(var i in levels) {
        var levelMandatory = currentLevels[levels[i].nodeRef] || false;

        var requirements = completeness.getLevelRequirements(levels[i]);
        for(var j in requirements) {
            var requirement = requirements[j];
            var scopes = requirement.assocs['req:requirementScope'] || [];
            var quantifier = requirement.properties['pred:quantifier'];
            var antecedents = requirement.childAssocs['pred:antecedent'] || [];

            //if(antecedents.length > 0) continue;

            var consequents = requirement.childAssocs['pred:consequent'] || [];
            for(var k in consequents) {
                var consequent = consequents[k];
                
                // process kind predicates
                if(consequent.isSubType('pred:kindPredicate')) {
                    if(quantifier == 'EXACTLY_ZERO') continue;
                    var documentKind = consequent.properties['pred:requiredKind'],
                        documentType = consequent.properties['pred:requiredType'],
                        kindModel = {
                            kind: documentKind || documentType,
                            type: documentType,
                            mandatory: (quantifier == 'EXACTLY_ONE' || quantifier == 'EXISTS') && levelMandatory,
                            multiple:  quantifier != 'EXACTLY_ONE' && quantifier != 'SINGLE'
                        };
                        var kindRef = "";
                        if(kindModel.kind)
                        kindRef = kindModel.kind.nodeRef + "";
                        var existingModel = index[kindRef];
                    if(existingModel) {
                        existingModel.mandatory = existingModel.mandatory || kindModel.mandatory;
                        existingModel.multiple = existingModel.multiple && kindModel.multiple;
                    } else {
                        index[kindRef] = kindModel;
                        kinds.push(kindModel);
                    }
                }
                
                // process levels required for subcases
                if(consequent.isSubType('req:requiredLevelsPredicate')) {
                    if(quantifier != 'EXACTLY_ZERO') continue;
                    if(scopes.length != 1 || scopes[0].name != 'subcases') continue;
                    if(consequent.properties["req:levelRequired"] != false) continue;
                    if(!levelMandatory) continue; // subcase level is mandatory only if this level is mandatory
                    
                    var subcaseLevels = consequent.assocs["req:requiredLevels"] || [];
                    for(var m in subcaseLevels) {
                        childLevels[subcaseLevels[m].nodeRef] = true;
                    }
                }
            }
        }
    }
    
    var documentKinds = kinds;
    
    model.containerKinds[containerKind.nodeRef] = {
        node: containerKind,
        documentKinds: documentKinds,
        childLevels: childLevels,
    };
    
    for(var i in documentKinds) {
        var documentKind = documentKinds[i].kind;
        var kindRef="";

        if(documentKind) kindRef = documentKind.nodeRef;
        if(model.documentKinds[kindRef]) continue;

        var documentType = documentKinds[i].type;
        var typeRef="";

        if(documentType) typeRef = documentType.nodeRef;
        model.documentKinds[kindRef] = {
            kind: documentKind,
            type: documentType
        };

        if(model.documentTypes[typeRef]) continue;
        model.documentTypes[typeRef] = documentType;
    }

}

function processContainer(model, container, currentLevels) {
    
    var items = container.children,
        containers = [],
        documents = [];

    for(var i in items) {
        if(items[i].hasAspect('icase:subcase')) {
            containers.push(items[i]);
        } else if(items[i].isDocument && items[i].hasAspect('tk:documentTypeKind')) {
            documents.push(items[i]);
        }
    }
    
    var containerKind = getContainerKind(container);
    processContainerKind(model, containerKind, currentLevels);
    var containerKindModel = model.containerKinds[containerKind.nodeRef];
    
    for(var i in documents) {
        var modifierName = documents[i].properties.modifier;
        if(model.people[modifierName]) continue;
        model.people[modifierName] = people.getPerson(modifierName);
    }
    
    var containersModel = [];
    for(var i in containers) {
        containersModel.push(processContainer(model, containers[i], containerKindModel.childLevels));
    }
    
    return {
        node: container,
        kind: containerKind,
        containers: containersModel,
        documents: documents
    };
}


function fillOurLove(model) {
    var rootNode = search.findNode("workspace://SpacesStore/category-document-type-root"),
        allTypes = rootNode.children;

    // check permission for create types
    model.permissions = {
       createType: rootNode.hasPermission("CreateChildren")
    }

    for (var t in allTypes) {
        var type = allTypes[t];

        if (!model.documentTypes[type.nodeRef]) {
            model.documentTypes[type.nodeRef] = type;
        }

        var allKinds = type.children;
        for (var k in allKinds) {
            var kind = allKinds[k];

            if (!model.documentKinds[kind.nodeRef]) {
                model.documentKinds[kind.nodeRef] = {
                    kind: kind,
                    type: type
                }
            }
        }
    }
    
    fillOurLoveRecursively(model, model.container);
}

function fillOurLoveRecursively(model, container) {
    
    for (var d in container.documents) {
        var doc = container.documents[d];
        var docKind = doc.properties["tk:kind"];
        if(!docKind) continue;

        var containerKindNode = getContainerKind(container.node);
        var containerKindNodeRef = containerKindNode.nodeRef;
        var containerKind = model.containerKinds[containerKindNodeRef];
        var kindExist = false;

        for (var k in containerKind.documentKinds) {
            var kind = containerKind.documentKinds[k].kind;
            if (kind.nodeRef.equals(docKind.nodeRef)) {
                kindExist = true;
                break;
            }
        }

        if (!kindExist) {
            containerKind.documentKinds.push({
                kind: doc.properties["tk:kind"],
                type: doc.properties["tk:type"],
                mandatory: false,
                multiple:  true
            })
        }
    }
    
    for (var m in container.containers) {
        fillOurLoveRecursively(model, container.containers[m]);
    }
}

function nodeMap(list) {
    var map = {};
    for(var i in list) {
        map[list[i].nodeRef] = true;
    }
    return map;
}

(function() {
    if(!args.nodeRef) {
        status.setCode(status.STATUS_BAD_REQUEST, "Argument 'nodeRef' should be specified");
        return;
    }

    var mainContainer = search.findNode(args.nodeRef);
    if(!mainContainer) {
        status.setCode(status.STATUS_NOT_FOUND, "Can not find node " + args.nodeRef);
        return;
    }
    
    var currentLevels = completeness.getCurrentLevels(mainContainer);
    
    model.containerKinds = {};
    model.documentTypes = {};
    model.documentKinds = {};
    model.people = {};
    model.stages = [];
    model.container = processContainer(model, mainContainer, nodeMap(currentLevels));

    fillOurLove(model);
    
})();