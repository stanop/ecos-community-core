<import resource="classpath:alfresco/site-webscripts/ru/citeck/utils/config-conditions.js">


function getWorkflowNames(workflows) {
	var workflowNames = [];
	if (!workflows) {
		return [];
	}
	// construct composite evaluator
	var evaluator = new CompositeEvaluator();
	evaluator.registerEvaluator(new GroupEvaluator());
	evaluator.registerEvaluator(new UserEvaluator());
	// try to add type and aspect evaluators:
	try {
		var selectedItem = page.url.args.selectedItems;
		var dao = new NodeDAO(selectedItem);
		evaluator.registerEvaluator(new TypeEvaluator(dao));
		evaluator.registerEvaluator(new AspectEvaluator(dao));
	} catch(e) {
		// invalid nodeRef
		// or no items are selected
		// or multiple items are selected
	}

	for (var hi = 0, hil = workflows.size(); hi < hil; hi++)
	{
		var a = workflows.get(hi);
		if(evaluator.evaluate(a))
		{
			workflowNames.push(a.attributes.name);
		}
	}
	return workflowNames;
}

// get hidden workflow names
function getHiddenWorkflowNames()
{
	var hiddenWorkflowsXML = config.scoped["Workflow"]["hidden-workflows"];
	if(!hiddenWorkflowsXML) {return [];}
	var hiddenWorkflows = hiddenWorkflowsXML.childrenMap["workflow"];
	return getWorkflowNames(hiddenWorkflows);
}

//get favorites workflow names
function getFavoritesWorkflowNames()
{
	var favoritesWorkflowsXML = config.scoped["Workflow"]["favorites-workflows"];
	if(!favoritesWorkflowsXML) {return [];}
	var favoritesWorkflows = favoritesWorkflowsXML.childrenMap["workflow"];
	return getWorkflowNames(favoritesWorkflows);
}
// get unable to create workflow names
function getUnableToCreateNames()
{
	var unableToCreateXML = config.scoped["Workflow"]["unable-to-create"];
	if(!unableToCreateXML) {return [];}
	var unableToCreate = unableToCreateXML.childrenMap["workflow"];
	return getWorkflowNames(unableToCreate);
}

function sortByTitle(workflow1, workflow2)
{
   var title1 = (workflow1.title || workflow1.name).toUpperCase(),
      title2 = (workflow2.title || workflow2.name).toUpperCase();
   return (title1 > title2) ? 1 : (title1 < title2) ? -1 : 0;
}

function getWorkflowDefinitions()
{
   var hiddenWorkflowNames = getHiddenWorkflowNames().concat(getUnableToCreateNames());
   var favoritesWorkflowNames = getFavoritesWorkflowNames();
      connector = remote.connect("alfresco"),
      result = connector.get("/api/workflow-definitions?exclude=" + hiddenWorkflowNames.join(","));

   if (result.status == 200)
   {
      var workflows = eval('(' + result + ')').data;
      workflows.sort(sortByTitle);      
      return workflows;
   }
   return [];
}

model.workflowDefs = getWorkflowDefinitions();
model.favoritesForkflow = getFavoritesWorkflowNames();
