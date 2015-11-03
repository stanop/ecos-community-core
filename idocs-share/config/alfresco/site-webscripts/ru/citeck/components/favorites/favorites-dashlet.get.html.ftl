<#assign id = args.htmlid?html>
<#assign id_js = id?js_string>
<script type="text/javascript">//<![CDATA[
(function()
{
   new Alfresco.widget.DashletResizer("${args.htmlid}", "${instance.object.id}");
   new Alfresco.widget.DashletTitleBarActions("${args.htmlid}").setOptions(
   {
      actions:
      [
         {
            cssClass: "help",
            bubbleOnClick:
            {
               message: "${msg("dashlet.help")?js_string}"
            },
            tooltip: "${msg("dashlet.help.tooltip")?js_string}"
         }
      ]
   });
})();
//]]>

</script>
<#assign favorites = ['activiti$perform', 'activiti$confirm']>
<div class="dashlet favorites">
	<div class="title">${msg("header")}</div>
	<div class="body scrollableList">
		<table border="0" width="100%" cellpadding="0" cellspacing="0">
			<#if workflowDefs?size != 0>
			    <#list workflowDefs as workflow>
			    	<#if favoritesForkflow?seq_contains(workflow.name)> 
					    <tr>
							<td class="detail-list-item first-item">
								<img src="${url.context}/res/components/documentlibrary/actions/citeck.doclib.action.start-idea-approval-wf-16.png"/>
							</td>
							<td class="detail-list-item first-item">
					            <a href="start-specified-workflow?workflowId=${workflow.name?html}">${workflow.title?html}</a>
							</td>						
						</tr>
					<#else>
						<tr></tr>
					</#if>			    
		        </#list>
			<#else><tr></tr></#if>
		</table>
	</div>
</div>