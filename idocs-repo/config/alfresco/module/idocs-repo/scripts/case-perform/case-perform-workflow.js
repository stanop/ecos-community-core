
function onWorkflowStart() {

}

function onTakeFlowBeforePerforming() {
    var initialPerformers = toJSArray(execution.getVariable("wfcp_performers")),
        excludedPerformers = toJSArray(execution.getVariable("excludedPerformers")),
        performers = [],
        performer;

    for (var i = 0; i < initialPerformers.length; i++) {
        performer = initialPerformers[i];
        if (excludedPerformers.indexOf(performer) == -1) {
            performers.push(performer);
        }
    }

    execution.setVariable("performers", toJavaList(performers));
}

function onPerformTaskCreated() {

}

function onPerformTaskAssigned() {

}

function onPerformTaskCompleted() {

    saveTaskResult();
    checkCommentConstraint();

    execution.setVariable("abortPerforming", isAllMandatoryPerformersDone()
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

/* ************************abort conditions***************************/
/*                                                                   */

function isAllMandatoryPerformersDone() {
    var optionalPerformers = toJSArray(execution.getVariable("optionalPerformers")),
        performers = toJSArray(execution.getVariable("performers")),
        outcomes = toJSArray(execution.getVariable("outcomes")),
        completedPerformers = getPerformersFromResults();

    if (performers.length == outcomes.length) {
        return true;
    }

    for (var i = 0; i < performers.length; i++) {
        var performer = performers[i];
        if (completedPerformers.indexOf(performer) == -1
                && optionalPerformers.indexOf(performer) == -1) {
            return false;
        }
    }
    return true;
}

function isAbortOutcomeReceived() {
    var outcome = task.getVariable("wfcp_performOutcome"),
        outcomesStr = execution.getVariable("wfcp_abortOutcomes") || "",
        outcomes = (outcomesStr + "").split(",");

    return arrayContains(outcomes, outcome);
}

/*                                                                   */
/* ************************abort conditions***************************/

function onWorkflowEnd() {

}

/* ******************************utils********************************/
/*                                                                   */

function saveTaskResult() {
    var outcome = task.getVariable("wfcp_performOutcome"),
        properties = {
            'wfcp:resultOutcome': outcome,
            'wfcp:resultDate': new Date()
        },
        name = getUniqueName(bpm_package, "perform-result-" + person.properties["cm:userName"]),
        candidate = task.getCandidates().toArray()[0],
        performerName;

    if (candidate) {
        if (candidate.isGroup()) {
            performer = people.getGroup(candidate.getGroupId());
        } else if (candidate.isUser()){
            performer = people.getPerson(candidate.getUserId());
        }
    } else {
        performer = people.getPerson(task.getAssignee());
    }

    var result = bpm_package.createNode(name, "wfcp:performResult", properties, "wfcp:performResults");

    result.createAssociation(person, "wfcp:resultPerson");
    if (performer) {
        result.createAssociation(performer, "wfcp:resultPerformer");
    }
}

function getUniqueName(parent, nameBase) {
    var counter = 1,
        name = nameBase;
    while (parent.childByNamePath(name)) {
        name = nameBase + ' (' + counter + ')';
        counter++;
    }
    return name;
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

function msg(key) {
    return Packages.org.springframework.extensions.surf.util.I18NUtil.getMessage(key);
}

function getPerformResults() {
    return bpm_package.childAssocs['wfcp:performResults'] || [];
}

function getPerformersFromResults() {
    var results = getPerformResults(),
        performers = [];

    for (var i = 0; i < results.length; i++) {
        performers = performers.concat(results[i].assocs['wfcp:resultPerformer'] || []);
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