<#assign id=args.htmlid?js_string>

<script type="text/javascript">
	new Citeck.widget.CaseStatus("${id}").setOptions({
		nodeRef: "${nodeRef}",
		isPendingUpdate: ${isPendingUpdate?string}
	}).setMessages(${messages});
</script>

<div id="${id}" class="case-status document-details-panel">
	<h2 id="${id}-heading" class="alfresco-twister">
		${msg("header.status")}<span class="panel-body"><@renderStatus/></span>
	</h2>
</div>

<#macro renderStatus>
	<#if isPendingUpdate>
    	<img style="vertical-align: middle" src="${url.context}/res/citeck/components/invariants/images/loading.gif"/>
	<#elseif status?has_content>${status}<#else>${msg("status.empty")}</#if>
</#macro>