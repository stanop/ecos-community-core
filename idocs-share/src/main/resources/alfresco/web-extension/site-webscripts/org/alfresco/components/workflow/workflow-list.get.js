<import resource="classpath:alfresco/site-webscripts/org/alfresco/components/workflow/workflow.lib.js">
<import resource="classpath:alfresco/site-webscripts/org/alfresco/components/workflow/filter/filter.lib.js">
<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function main() {
    model.workflowDefinitions = getWorkflowDefinitions();
    model.hiddenWorkflowNames = getHiddenWorkflowNames();
    model.filterParameters = getFilterParameters();
    model.maxItems = getMaxItems();

    AlfrescoUtil.param("isCompleted", false);

    //Widget instantiation metadata...
    var workflowList = {
        id: "CompletedWorkflowList",
        name: "Citeck.CompletedWorkflowList",
        options: {
            filterParameters: model.filterParameters,
            hiddenWorkflowNames: model.hiddenWorkflowNames,
            workflowDefinitions: model.workflowDefinitions,
            maxItems: parseInt((model.maxItems != null) ? model.maxItems : "50"),
            isCompleted: model.isCompleted
        }
    };
    model.widgets = [workflowList];
}

main();

