<#escape x as x?html>
<html>
   <head>
      <style type="text/css"></style>
   </head>
   <body bgcolor="white">
		<div style="font-size: 14px; margin: 0px 0px 0px 0px; padding-top: 0px; border-top: 0px solid #aaaaaa;"> 
				<p> Задача
                 <#assign taskUrl = shareUrl+"/page/task-edit-with-preview?taskId="+args.task.id/>
                 <#if args.workflow.documents?? && args.workflow.documents?size != 0><#assign taskUrl = taskUrl+"&nodeRef="+args.workflow.documents[0].nodeRef/></#if>
                 <p><a href="${taskUrl}">${taskUrl}</a> </p>
				передана в пул.<br></p> 
		</div>
   </body>
</html>
</#escape>