<html>
   <head>
      <style type="text/css"></style>
   </head>
   <body bgcolor="white">
		<div style="font-size: 14px; margin: 0px 0px 0px 0px; padding-top: 0px; border-top: 0px solid #aaaaaa;"> 
			Сотрудник <u>${args.task.editor}</u> ознакомился с:
			<#if args.workflow.documents??>
                <div style="font-size: 14px; margin: 0px 0px 0px 0px; padding-top: 0px; border-top: 0px solid #aaaaaa;">
                    ${args.workflow.documents.properties["cm:name"]}
                </div>
			</#if>
			<#if args.task.properties.bpm_comment??>
				Замечания: ${args.task.properties.bpm_comment}.
			</#if>
			<div style="font-size: 14px; margin: 0px 0px 0px 0px; padding-top: 10px; border-top: 0px solid #aaaaaa;">
				Ссылка на задачу ознакомления: <a href="${shareUrl}/page/task-details?taskId=activiti$${args.task.id}"><u>${shareUrl}/page/task-details?taskId=activiti$${args.task.id}</u></a>
			</div>
		</div>
   </body>
</html>