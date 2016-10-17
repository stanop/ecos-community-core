
const OPTIONAL_PERFORMERS = 'optionalPerformers';
const EXCLUDED_PERFORMERS = 'excludedPerformers';
const TASKS_PERFORMERS = 'tasksPerformers';
const MANDATORY_TASKS = 'mandatoryTasks';

function onWorkflowStart() {
    if (execution.hasVariable(OPTIONAL_PERFORMERS)) {
        var optionalPerformers = convertForJS(execution.getVariable(OPTIONAL_PERFORMERS));
        var expandedPerformers = [];
        forEach(optionalPerformers, function(p) {
            expandedPerformers.push(p);
            expandedPerformers = expandedPerformers.concat(people.getMembers(p, true));
        });
        execution.setVariable(OPTIONAL_PERFORMERS, convertForRepo(expandedPerformers));
    }
}

function onBeforePerformingFlowTake() {
    var initialPerformers = toJSArray(execution.getVariable("wfcp_performers")),
        excludedPerformers = toJSArray(getExecutionList(EXCLUDED_PERFORMERS)),
        performers = [];

    forEach(initialPerformers, function(performer) {
        if (excludedPerformers.indexOf(performer) == -1) {
            performers.push(performer);
        }
    });

    execution.setVariable("performers", toJavaList(performers));
}

function onAfterPerformingFlowTake() {

}

function onSkipPerformingFlowTake() {

}

function onSkipPerformingGatewayStarted() {
    var optionalPerformers = getExecutionList(OPTIONAL_PERFORMERS),
        performers = execution.getVariable("performers"),
        hasMandatoryTasks = false;

    for (var i = 0; i < performers.size(); i++) {
        var performer = performers.get(i);
        if (!optionalPerformers.contains(performer)) {
            hasMandatoryTasks = true;
            break;
        }
    }

    execution.setVariable("skipPerforming", performers.size() == 0 || !hasMandatoryTasks);
}

function onPerformTaskCreated() {

    var variables = execution.getVariables();
    for (var variable in variables) {
        var value = variables[variable];
        if (isSharedVariable(variable)) {
            task.setVariable(variable, value);
        }
    }

    saveTaskPerformers();

    var optionalPerformers = getExecutionList(OPTIONAL_PERFORMERS),
        performers = getTaskPerformers(),
        isOptional = containsAll(optionalPerformers, extractNodeRefs(performers));

    task.setVariableLocal('cwf_isOptionalTask', isOptional);
    if (!isOptional) {
        var mandatoryTasks = getExecutionList(MANDATORY_TASKS);
        mandatoryTasks.add("task$" + task.getId());
    }
}

function onPerformTaskAssigned() {
}

function onPerformTaskCompleted() {

    var variables = task.getVariables();
    for (var variable in variables) {
        var value = variables[variable];
        if (isSharedVariable(variable)) {
            execution.setVariable(variable, value);
        }
    }

    saveTaskResult();
    checkCommentConstraint();

    var mandatoryTasks = getExecutionList(MANDATORY_TASKS);
    mandatoryTasks.remove("task$" + task.getId());

    execution.setVariable("abortPerforming", mandatoryTasks.size() == 0
                                             || isAbortOutcomeReceived());

}

function checkCommentConstraint() {
    var outcome = task.getVariable("wfcp_performOutcome"),
        outcomesStr = execution.getVariable("wfcp_outcomesWithMandatoryComment") || "",
        outcomes = (outcomesStr + "").split(",");

    if (arrayContains(outcomes, outcome)) {
        var comment = task.getVariable("bpm_comment");
        if (!comment || (comment + "").length == 0) {
             throw(msg("wfcf_confirmworkflow.message_comment_is_empty"));
        }
    }
}

function isAbortOutcomeReceived() {
    var outcome = task.getVariable("wfcp_performOutcome"),
        outcomesStr = execution.getVariable("wfcp_abortOutcomes") || "",
        outcomes = (outcomesStr + "").split(",");

    return arrayContains(outcomes, outcome);
}

function onWorkflowEnd() {
    clearList(OPTIONAL_PERFORMERS);
    clearList(EXCLUDED_PERFORMERS);
}

/* ******************************utils********************************/
/*                                                                   */

function saveTaskResult() {
    var taskVariables = task.getVariables(),
        outcome = taskVariables["wfcp_performOutcome"],
        properties = {
            'wfcp:resultOutcome': outcome,
            'wfcp:resultDate': new Date()
        },
        name = "perform-result-" + person.properties["cm:userName"],
        performersByTask = convertForJS(execution.getVariable(TASKS_PERFORMERS)) || {},
        performers = performersByTask["task$" + task.getId()] || getTaskPerformers();

    var result = bpm_package.createNode(name, "wfcp:performResult", properties, "wfcp:performResults");

    result.createAssociation(person, "wfcp:resultPerson");

    forEach(performers, function(performer) {
        result.createAssociation(performer, "wfcp:resultPerformer");
    });
}

function saveTaskPerformers() {
    var tasksPerformers = convertForJS(execution.getVariable(TASKS_PERFORMERS)) || {},
        performers = tasksPerformers[task.getId()] || [],
        performersFromTask = getTaskPerformers();

    forEach(performersFromTask, function(p) {
        var contains = false;
        for (var performer in performers) {
            if (performer.equals(p)) {
                contains = true;
                break;
            }
        }
        if (!contains) {
            performers.push(p);
        }
    });

    tasksPerformers["task$" + task.getId()] = performers;
    execution.setVariable(TASKS_PERFORMERS, convertForRepo(tasksPerformers));
}

function isSharedVariable(name) {
    var transferableVars = [],
        ignoredVars = [],
        ignoredPrefixes = ['bpm', 'cwf', 'wfcf', 'cm'],
        prefix = (name.match(/^([^_]+)_.+/) || [])[1];

    return arrayContains(transferableVars, name)
           || !arrayContains(ignoredVars, name)
              && prefix != undefined
              && !arrayContains(ignoredPrefixes, prefix);
}

function toJSArray(list) {
    var result = [];
    if (!list) {
        return result;
    }
    for (var i = 0; i < list.size(); i++) {
        result.push(list.get(i));
    }
    return result;
}

function toJavaList(arr) {
    var result = new Packages.java.util.ArrayList();
    if (!arr) {
        return result
    }
    for (var i = 0; i < arr.length; i++) {
        result.add(arr[i]);
    }
    return result;
}

function clearList(varName) {
    var variable = execution.getVariable(varName);
    if (variable) {
        variable.clear();
    }
}


function msg(key) {
    return Packages.org.springframework.extensions.surf.util.I18NUtil.getMessage(key);
}

function getPerformResults() {
    return bpm_package.childAssocs['wfcp:performResults'] || [];
}

function getPerformersFromResults() {
    var results = getPerformResults(),
        performers = new Packages.java.util.ArrayList(),
        performer;

    for (var i = 0; i < results.length; i++) {
        performer = (results[i].assocs['wfcp:resultPerformer'] || [])[0];
        if (performer) performers.add(performer);
    }

    return performers;
}

function arrayContains(arr, value) {
    for each(var val in arr) {
        if (val == value) {
            return true;
        }
    }
    return false;
}

function getTaskPerformers() {
    var candidates = task.getCandidates(),
        performers = new Packages.java.util.ArrayList(),
        assignee = task.getAssignee();

    if (assignee) {
        performers.add(people.getPerson(assignee));
    }

    forEach(candidates, function(candidate) {
        if (candidate.isGroup()) {
            performers.add(people.getGroup(candidate.getGroupId()));
        } else if (candidate.isUser()){
            performers.add(people.getPerson(candidate.getUserId()));
        }
    });

    return performers;
}

function containsAll(list, elements) {
    var result = true;
    forEach(elements, function(element) {
        if (result && !list.contains(element)) {
            result = false;
        }
    });
    return result;
}

function forEach(arr, func) {
    if (arr == null) return;
    if (arr.iterator) {
        var it = arr.iterator();
        while (it.hasNext()) {
            func(it.next());
        }
    } else {
        for each(var val in arr) {
            func(val);
        }
    }
}

function extractNodeRefs(arr) {
    var result = new Packages.java.util.ArrayList();
    forEach(arr, function(it) {
        result.add(it.nodeRef);
    });
    return result;
}

function getExecutionList(name) {
    var list = execution.getVariable(name);
    if (!list) {
        list = new Packages.java.util.ArrayList();
        execution.setVariable(name, list);
    }
    return list;
}

function convertForRepo(value) {
    if (value == null) {
        return null;
    }
    var converter = new Packages.org.alfresco.repo.jscript.ValueConverter();
    return converter.convertValueForRepo(value);
}

function convertForJS(value) {
    if (value == null) {
        return null;
    }

    if (value instanceof Packages.java.util.Map) {
        var result = {};
        for (var key in value){
            result[key] = convertForJS(value[key]);
        }
        return result;
    }

    var converter = new Packages.org.alfresco.repo.jscript.ValueConverter();
    return converter.convertValueForScript(services.get("ServiceRegistry"), {}, null, value);
}