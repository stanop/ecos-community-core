<import resource="classpath:alfresco/site-webscripts/org/alfresco/components/workflow/workflow.lib.js">

function main()
{
   // Widget instantiation metadata...
   var taskEditHeader = {
      id : "TaskEditHeader", 
      name : "Citeck.TaskEditHeader",
      options : {
         submitButtonMessageKey : "button.saveandclose",
         defaultUrl : getSiteUrl("my-tasks")
      }
   };
   model.widgets = [taskEditHeader];
}

main();
