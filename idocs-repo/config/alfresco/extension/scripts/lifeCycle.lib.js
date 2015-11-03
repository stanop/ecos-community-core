function startWorkflow(workflowName, parameters) {
    var action = actions.create("start-workflow");
    var wfPackage = workflow.createPackage();

    action.parameters.workflowName = workflowName;
    action.parameters["bpm:workflowPackage"] = wfPackage.nodeRef;
    action.parameters["initiator"] = person.nodeRef;
    action.parameters["bpm:workflowPriority"] = document.properties["cwf:priority"] ? document.properties["cwf:priority"] : 2;
    action.parameters["bpm:sendEMailNotifications"] = false;
    action.parameters["bpm:workflowDescription"] = document.name;

    var converter = new Packages.ru.citeck.ecos.lifecycle.LifeCycleValueConverter();
    for (var parameter in parameters) {
       if(!parameters.hasOwnProperty(parameter)) continue;
       action.parameters[parameter] = converter.convertValueForJava(parameters[parameter]);
    }
    action.execute(document.nodeRef);

    // reset document info, because it could be changed, while starting workflow
    document.reset();
}

function getActiveWorkflows(workflowName) {
    var workflowService = services.get("WorkflowService");
    var workflows = workflowService.getWorkflowsForContent(document.nodeRef, true).toArray();
    var result = [];
    for(var i in workflows) {
        if(!workflowName || workflowName == "" + workflows[i].definition.name) {
            result.push(workflow.getInstance(workflows[i].id));
        }
    }
    return result;
}

function getCompletedWorkflows(workflowName) {
    var workflowService = services.get("WorkflowService");
    var workflows = workflowService.getWorkflowsForContent(document.nodeRef, false).toArray();
    var result = [];
    for(var i in workflows) {
        if(!workflowName || workflowName == "" + workflows[i].definition.name) {
            result.push(workflow.getInstance(workflows[i].id));
        }
    }
    return result;
}

function sendEmail(params) {
	try {
		var mail = actions.create("mail");
		mail.parameters.subject = params.subject;
		mail.parameters.to_many = params.to;
		if (params.template) {
			var node = search.selectNodes('/app:company_home/app:dictionary/app:email_templates/' + params.template)[0];
			if (node) {
				mail.parameters.template = node;
			}
		}
	if(params.document)
		mail.parameters.document = params.document;
		mail.parameters.text = params.text || params.subject;
		if (params.now) {
			mail.execute(document);
		} else {
			mail.executeAsynchronously(document);
		}
	}
	catch(e) {
		logger.log('Can not send e-mail: to: ' + params.to +
				'; subject: ' + params.subject +
				'; template: ' + params.template +
				'; text: ' + params.text +
				'; now: ' + params.now +
				'; exception: ' + e);
	}
}

function translateDaysNumberToDate(daysNumber) {
    var today = new Date();
    var newDate = new Date(today);
    newDate.setDate(today.getDate() + daysNumber);
    return newDate;
}

function inGroup() {
    var personGroups = people.getContainerGroups(person);
    for (var i in personGroups) {
        if (!personGroups.hasOwnProperty(i))
            continue;
        for (var j = 0; j < arguments.length; j++) {
            var group = people.getGroup(arguments[j]);
            if (group) {
                if (personGroups[i].equals(group)) {
                    return true;
                }
            }
        }
    }
    return false;
}

var inGroups = inGroup;

function getTaskForWorkflow(workflowName) {
    var workflowService = services.get("WorkflowService");
    var workflows = workflowService.getWorkflowsForContent(document.nodeRef, true).toArray();
    var workflow = [];
	var resultTasks = [];
    for(var i in workflows) 
	{
        if(!workflowName || workflowName == "" + workflows[i].definition.name) 
		{
            var workflowPaths = workflowService.getWorkflowPaths(workflows[i].id);
			for(var i=0; i<workflowPaths.size();i++)
			{
				if(workflowPaths.get(i)) 
				{
					var path = workflowPaths.get(i); 
					var tasks = workflowService.getTasksForWorkflowPath(path.getId());
					for(var m=0; m<tasks.size(); m++)
					{
						if(tasks.get(m)) 
						{
							resultTasks.push(tasks.get(m));
						}
					}
				}
			}
		}
	}
	return resultTasks;
}

function completeTask(task, transitionId) {
    var workflowService = services.get("WorkflowService");
	workflowService.endTask(task.id, transitionId);
}
function containsChildrenWithType(type) {
	var docs = document.childAssocs["cm:contains"];
	for (var i = 0; i < docs.length; i++) {
		if(docs[i].getTypeShort().equals(type))
		{
			return true;
		}
	}    
	return false;
}

function getTaskForWorkflow(workflowName) {
    var workflowService = services.get("WorkflowService");
    var workflows = workflowService.getWorkflowsForContent(document.nodeRef, true).toArray();
    var workflow = [];
	var resultTasks = [];
    for(var i in workflows) 
	{
        if(!workflowName || workflowName == "" + workflows[i].definition.name) 
		{
            var workflowPaths = workflowService.getWorkflowPaths(workflows[i].id);
			for(var i=0; i<workflowPaths.size();i++)
			{
				if(workflowPaths.get(i)) 
				{
					var path = workflowPaths.get(i); 
					var tasks = workflowService.getTasksForWorkflowPath(path.getId());
					for(var m=0; m<tasks.size(); m++)
					{
						if(tasks.get(m)) 
						{
							resultTasks.push(tasks.get(m));
						}
					}
				}
			}
		}
	}
	return resultTasks;
}

function completeTask(task, transitionId) {
    var workflowService = services.get("WorkflowService");
	workflowService.endTask(task.id, transitionId);
}

function getSystemConfig(configKey) {
	var nodes = search.luceneSearch('TYPE\:"config:ecosConfig" AND @config\\:key:"'+configKey+'"');
	var configValue;
	if (nodes.length!=0){

		containerNode = nodes[0].nodeRef;
		if(nodes[0].properties["config:value"]){
			configValue = nodes[0].properties["config:value"];
		}
	}
	return configValue;
}
function hasPermissionsToStartProcess() {
    if (availability.getUserAvailability(document.properties.creator)) {
        return document.properties.creator == person.properties.userName;
    } else {
        var delegates = delegates.getUserDelegates(document.properties.creator);
        for (var i = 0; i < delegates.length; i++) {
            if (delegates[i].userName == person.properties.userName) {
                return true;
            }
        }
        return document.properties.creator == person.properties.userName;
    }
}

function getParentWithType(type) {
	var docs = document.parentAssocs["cm:contains"];
	var parent;
	if(docs && docs.length>0)
	{
		for (var i = 0; i < docs.length; i++) {
			if(docs[i].getTypeShort().equals(type))
			{
				parent = docs[i];
				return parent;
			}
		}
	}
	return parent;
}

function checkCompletenessLevel(doc) {
	if(doc.assocs)
	{
		var completedLevels = doc.assocs["req:completedLevels"];
		if(completedLevels && completedLevels.length>0)
		{
			for (var i = 1; i < arguments.length; i++) {
				var requiredLevel = arguments[i];
				var levelPass = false;
				for (var j = 0; j < completedLevels.length; j++) {
					if (completedLevels[j].name.equals(requiredLevel)) {
						levelPass = true;
					}
				}
				if(!levelPass)
				{
					return false;
				}
			}
			return true;
		}
		else
		{
			return false;
		}
	}
	else
	{
		return false;
	}
}
