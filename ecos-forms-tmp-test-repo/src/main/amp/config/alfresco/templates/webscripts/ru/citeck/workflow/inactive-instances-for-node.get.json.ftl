<#-- Workflow Instances collection for NodeRef-->

<#import "/org/alfresco/repository/workflow/workflow.lib.ftl" as workflowLib />
{
   "data": 
   [
      <#list workflowInstances as workflowInstance>
      <@workflowLib.workflowInstanceJSON workflowInstance=workflowInstance />
      <#if workflowInstance_has_next>,</#if>
      </#list>
   ]
}