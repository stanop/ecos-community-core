
function createOrUpdateLevels(models) {

    var levelsRoot = search.selectNodes("/cm:IDocsRoot/journal:journalMetaRoot/cm:journals/cm:case-completeness-levels/journal:default")[0].assocs["journal:destination"][0];
    var defaultScope = search.selectNodes("/app:company_home/app:dictionary/cm:case-element-configs/cm:documents")[0];

    for(var i in models) {
        var model = models[i];
        var titles = model.titles;
        if (!titles || titles.length == 0) titles = {locale: "en", value: model.name};

        var level;
        if (model.uuid) {
            logger.warn("Try to find completeness level with uuid=\"" + model.uuid + "\"");
            level = search.findNode("workspace://SpacesStore/" + model.uuid);
        } else {
            logger.warn("Try to find completeness level with name=\"" + model.name + "\"");
            level = levelsRoot.childByNamePath(model.name);
        }

        if(!level) {
            logger.warn("+ Not found. Creating...");

            properties = {
                "cm:name": model.name
            };
            if (model.uuid) {
                properties["sys:node-uuid"] = model.uuid;
            }

            level = levelsRoot.createNode(null, "req:completenessLevel", properties, "cm:contains", "cm:"+model.name);
            for(var ti in titles) {
                utils.setLocale(titles[ti].locale);
                level = search.findNode(level.nodeRef);
                level.properties["cm:title"] = titles[ti].value;
            }
            level.save();
        } else {
            logger.warn("- Found. Updating.");
            level.properties['cm:name'] = model.name;

            for(var ti in titles) {
                utils.setLocale(titles[ti].locale);
                level = search.findNode(level.nodeRef);
                level.properties["cm:title"] = titles[ti].value;
            }
            level.save();
        }

        for(var j in model.req) {
            var req = model.req[j];
            if(!req.predicate) req.predicate = "requirement";
            createPredicate(level, "req:levelRequirement", req, defaultScope);
        }
    }
}

function createPredicate(parent, assocType, data, defaultScope) {
    var predicate, properties = {};

    if(data.name) {
        logger.warn("\tTry to find predicate \""+data.name+"\"");
        predicate = parent.childByNamePath(data.name);
        if(!predicate && data.consequent.predicate == "kind") {
            var requiredKind = data.consequent.requiredKind;
            var predicates = parent.childAssocs[assocType] || [];
            for(var i in predicates) {
                var candidate = predicates[i];
                if(candidate.isSubType('pred:kindPredicate') && candidate.properties['pred:requiredKind'] == requiredKind) {
                    predicate = candidate;
                    break;
                }
            }
        }
        logger.warn(predicate ? "\tFound." : "\tNot found.");
    }

    if(!predicate) {
        logger.warn("\t + Creating...");

        switch (data.predicate) {
            case 'kind':

                properties["pred:requiredType"] = data.requiredType;
                if (data.requiredKind) {
                    properties["pred:requiredKind"] = data.requiredKind;
                }

                predicate = parent.createNode(null, "pred:kindPredicate", properties, assocType);
                break;

            case 'requiredLevels':
                predicate = parent.createNode(null, "req:requiredLevelsPredicate", null, assocType);

                if(!data.level) {
                    throw "Error! 'level' is mandatory parameter in 'requiredLevels' predicate";
                }
                var levelsRoot = search.selectNodes("/app:company_home/app:dictionary/cm:case-completeness-levels")[0];
                var level = levelsRoot.childByNamePath(data.level);
                if(!level) {
                    throw "Error! Level '"+data.level+"' doesn't found";
                }
                predicate.createAssociation(level, "req:requiredLevels");

                if(typeof data.levelRequired !== 'undefined') predicate.properties['req:levelRequired'] = data.levelRequired;
                break;

            case 'subcaseType':

                if(data.subcaseType) properties["req:requiredSubcaseType"] = utils.longQName(data.subcaseType);
                if(data.elementType) properties["req:requiredElementType"] = utils.longQName(data.elementType);
                predicate = parent.createNode(null, "req:subcaseTypePredicate", properties, assocType);
                break;

            case 'javascript':

                predicate = parent.createNode(null, "pred:javascriptPredicate", {"pred:javascriptExpression": data.expression}, assocType);
                break;

            case 'requirement':

                predicate = parent.createNode(null, "req:requirement", {
                    "cm:title": data.name,
                    "cm:name": data.name,
                    "pred:quantifier": data.quantifier || "EXISTS"
                }, assocType);

                if(data.antecedent) {
                    if(data.antecedent.constructor === Array) {
                        for(var i in data.antecedent) {
                            createPredicate(predicate, "pred:antecedent", data.antecedent[i], defaultScope);
                        }
                    } else {
                        createPredicate(predicate, "pred:antecedent", data.antecedent, defaultScope);
                    }
                }

                if(!data.consequent) {
                    throw "Consequent association is mandatory!";
                }
                if(data.consequent.constructor === Array) {
                    for(var i in data.consequent) {
                        createPredicate(predicate, "pred:consequent", data.consequent[i], defaultScope);
                    }
                } else {
                    createPredicate(predicate, "pred:consequent", data.consequent, defaultScope);
                }

                if(data.scope) {
                    var scope = search.selectNodes("/app:company_home/app:dictionary/cm:case-element-configs/cm:"+data.scope)[0];
                    predicate.createAssociation(scope, "req:requirementScope");
                } else if(typeof data.scope == "undefined") {
                    predicate.createAssociation(defaultScope, "req:requirementScope");
                }
                break;
        }
    }

    if(predicate) {
        if (data.titles) {
            var titles = data.titles;
            for(var ti in titles) {
                utils.setLocale(titles[ti].locale);
                predicate = search.findNode(predicate.nodeRef);
                predicate.properties["cm:title"] = titles[ti].value;
                predicate.save();
            }
        }
        if (data.name) predicate.name = data.name;

        for (var prop in properties) {
            predicate.properties[prop] = properties[prop];
        }

        predicate.save();
    }
}
