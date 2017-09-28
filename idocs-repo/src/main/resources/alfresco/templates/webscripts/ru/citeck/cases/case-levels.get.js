function contains(map, element) {
    return map[element.nodeRef] == true;
}

function createNodeRefMap(nodes) {
    var map = {};
    for(var i in nodes) {
        map[nodes[i].nodeRef] = true;
    }
    return map;
}

(function() {
    
    var caseNode = search.findNode(args.nodeRef);
    if(!caseNode) {
        status.setStatus(status.STATUS_NOT_FOUND, "Can not find case '" + args.nodeRef + "'");
        return;
    }
    
    var allLevels = completeness.getAllLevels(caseNode),
        currentLevels = createNodeRefMap(completeness.getCurrentLevels(caseNode)),
        completedLevels = createNodeRefMap(completeness.getCompletedLevels(caseNode)),
        passedRequirements = createNodeRefMap(completeness.getPassedRequirements(caseNode));
    
    var levelModels = [];
    for(var i in allLevels) {
        var levelNode = allLevels[i],
            current = contains(currentLevels, levelNode),
            completed = contains(completedLevels, levelNode),
            requirements = completeness.getLevelRequirements(levelNode);
        
        var requirementModels = [];
        for(var j in requirements) {
            var requirementNode = requirements[j],
                passed = contains(passedRequirements, requirementNode),
                matches = completeness.getMatchedElements(caseNode, requirementNode);
            
            requirementModels.push({
                node: requirementNode,
                passed: passed,
                matches: matches
            });
        }
        
        levelModels.push({
            node: levelNode,
            current: current,
            completed: completed,
            requirements: requirementModels
        });
    }
    
    model.caseNode = caseNode;
    model.levels = levelModels;
    
})()