
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
    var predicate, properties = {}, assocs = {}, type, children = [];

    switch (data.predicate) {
        case 'kind':

            type = "pred:kindPredicate";

            properties["pred:requiredType"] = data.requiredType;
            properties["pred:requiredKind"] = data.requiredKind || null;

            break;

        case 'requiredLevels':

            type = "req:requiredLevelsPredicate";

            if(!data.level) {
                throw "Error! 'level' is mandatory parameter in 'requiredLevels' predicate";
            }
            var levelsRoot = search.selectNodes("/app:company_home/app:dictionary/cm:case-completeness-levels")[0];
            var level = levelsRoot.childByNamePath(data.level);
            if(!level) {
                throw "Error! Level '"+data.level+"' doesn't found";
            }
            if (!assocs["req:requiredLevels"]) assocs["req:requiredLevels"] = [];
            assocs["req:requiredLevels"].push(level);

            if(typeof data.levelRequired !== 'undefined') properties['req:levelRequired'] = data.levelRequired;

            break;

        case 'subcaseType':

            type = "req:subcaseTypePredicate";

            if(data.subcaseType) properties["req:requiredSubcaseType"] = utils.longQName(data.subcaseType);
            if(data.elementType) properties["req:requiredElementType"] = utils.longQName(data.elementType);

            break;

        case 'javascript':

            type = "pred:javascriptPredicate";
            properties["pred:javascriptExpression"] = data.expression;

            break;

        case 'requirement':

            type = "req:requirement";

            properties["cm:title"] = data.name;
            properties["pred:quantifier"] = data.quantifier || "EXISTS";

            if (!children["pred:antecedent"]) children["pred:antecedent"] = [];

            if(data.antecedent) {
                if(data.antecedent.constructor === Array) {
                    for(var i in data.antecedent) {
                        children["pred:antecedent"].push(data.antecedent[i]);
                    }
                } else {
                    children["pred:antecedent"].push(data.antecedent);
                }
            }

            if(!data.consequent) {
                throw "Consequent association is mandatory!";
            }

            if (!children["pred:consequent"]) children["pred:consequent"] = [];

            if(data.consequent.constructor === Array) {
                for(var i in data.consequent) {
                    children["pred:consequent"].push(data.consequent[i]);
                }
            } else {
                children["pred:consequent"].push(data.consequent);
            }

            if (!assocs["req:requirementScope"]) assocs["req:requirementScope"] = [];

            if(data.scope) {
                var scope = search.selectNodes("/app:company_home/app:dictionary/cm:case-element-configs/cm:"+data.scope)[0];
                assocs["req:requirementScope"].push(scope);
            } else if(typeof data.scope == "undefined") {
                assocs["req:requirementScope"].push(defaultScope);
            }

            break;
    }

    if (data.name) {
        properties['cm:name'] = data.name;
        predicate = parent.childByNamePath(data.name);
    }

    if (!predicate && data.predicate == "kind") {
        var requiredType = data.requiredType ? search.findNode(data.requiredType) : null;
        var requiredKind = data.requiredKind ? search.findNode(data.requiredKind) : null;
        var predicates = parent.childAssocs[assocType] || [];

        for (var i in predicates) {
            var candidate = predicates[i];

            if (candidate.isSubType('pred:kindPredicate')
                && Packages.java.util.Objects.equals(candidate.properties['pred:requiredType'], requiredType)
                && Packages.java.util.Objects.equals(candidate.properties['pred:requiredKind'], requiredKind)) {

                predicate = candidate;
                break;
            }
        }
    }

    if (!predicate) {
        predicate = parent.createNode(null, type, properties, assocType);
    } else {
        for (var prop in properties) {
            predicate.properties[prop] = properties[prop];
        }
    }

    setTitles(predicate, data.titles);

    for (var aType in assocs) {
        setAssocs(predicate, aType, assocs[aType]);
    }

    for (var aType in children) {
        var childNodes = [];
        var data = children[aType];
        for (var i in data) {
            childNodes.push(createPredicate(predicate, aType, data[i], defaultScope));
        }
        var diff = getNodesDifference(predicate.childAssocs[aType] || [], childNodes);
        for (var i in diff.removed) {
            diff.removed[i].remove();
        }
    }

    predicate.save();

    return predicate;
}

function setAssocs(node, assocType, targets) {
    var diff = getNodesDifference(node.assocs[assocType] || [], targets);
    for (var i in diff.removed) {
        node.removeAssociation(diff.removed[i], assocType);
    }
    for (var i in diff.added) {
        node.createAssociation(diff.added[i], assocType);
    }
}

function getNodesDifference(base, target) {
    var added = [], removed = [];
    for (var i = 0; i < base.length; i++) {
        if (!containsNode(target, base[i])) {
            removed.push(base[i]);
        }
    }
    for (var i = 0; i < target.length; i++) {
        if (!containsNode(base, target[i])) {
            added.push(target[i]);
        }
    }
    return {'added': added, 'removed': removed};
}

function containsNode(array, node) {
    for (var i = 0; i < array.length; i++) {
        if (array[i].equals(node)) {
            return true;
        }
    }
    return false;
}

function setTitles(node, titles) {
    if (!titles) return;
    for (var ti in titles) {
        utils.setLocale(titles[ti].locale);
        node.properties["cm:title"] = titles[ti].value;
        node.save();
    }
}
