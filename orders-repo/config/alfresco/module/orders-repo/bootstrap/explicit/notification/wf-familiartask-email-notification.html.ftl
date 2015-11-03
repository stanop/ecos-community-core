<html>
   <head>
      <style type="text/css"></style>
   </head>
   <body bgcolor="white">
		<div style="font-size: 14px; margin: 0px 0px 0px 0px; padding-top: 0px; border-top: 0px solid #aaaaaa;"> 
			<div style="font-size: 14px; margin: 0px 0px 0px 0px; padding-top: 0px; border-top: 0px solid #aaaaaa;"> 
			<#if args.workflow.documents?? && args.workflow.documents?size != 0>
			    <#list args.workflow.documents as doc>
                    Вам необходимо ознакомиться с документом ${doc.properties["orders:header"]}
			    </#list>
			</#if>                   
			<#if (args.task.properties.bpm_description)??>
				, сообщение: "${args.task.properties.bpm_description}".
			</#if>
			</div>
			<div style="font-size: 14px; margin: 0px 0px 0px 0px; padding-top: 10px; border-top: 0px solid #aaaaaa;"> 
				Перейти к заданию можно по ссылке: <a href="${shareUrl}/page/task-edit?taskId=activiti$${args.task.id}"><u>${shareUrl}/page/task-edit?taskId=activiti$${args.task.id}</u></a></p>
			</div>
		</div>
   </body>
</html>