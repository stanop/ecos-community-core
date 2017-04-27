
if (args['nodeRef']) {
    model.result = relatedWorkflowService.getRelatedWorkflowsForNode(args['nodeRef']);
} else {
    model.result = relatedWorkflowService.getRelatedWorkflowsForNode(
        args['store_type'],
        args['store_id'],
        args['id']
    );
}

