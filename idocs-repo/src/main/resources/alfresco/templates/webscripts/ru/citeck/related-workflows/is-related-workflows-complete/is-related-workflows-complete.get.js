
model.result = 'true';

var task = workflow.getTask(args['taskId']);
relatedWorkflows = task.getProperties()['rwf:relatedWorkflows'];
if (!relatedWorkflows) {
    relatedWorkflows = '';
}

var ids = relatedWorkflows.split(',');
for (var i in ids) {
    if (ids[i]) {
        var inst = workflow.getInstance(ids[i]);
        if (inst && inst.isActive()) {
            model.result = 'false';
            break;
        }
    }
}


