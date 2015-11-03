(function() {

// get all workflow definitions:
var result = remote.call('/api/workflow-definitions');
if(result.status != 200) {
	throw "Problem connecting to repository";
}
definitions = eval('(' + result + ')');

if(!definitions || !definitions.data || !definitions.data.length) {
	status.setCode(status.STATUS_NOT_FOUND, "No workflow definitions found");
	return;
}
definitions = definitions.data;

var workflowDef = null;
for(var i = 0; i < definitions.length; i++) {
	if(definitions[i].name == args.workflowId) {
		workflowDef = definitions[i];
		break;
	}
}

if(!workflowDef) {
	status.setCode(status.STATUS_NOT_FOUND, "Workflow definition " + args.workflowId + " not found");
	return;
}

model.workflow = workflowDef;

})();