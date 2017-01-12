<#assign id=args.htmlid?js_string>
   <script type="text/javascript">//<![CDATA[
   new Citeck.widget.DocumentStatus("${id}").setOptions(
   {
      nodeRef: "${nodeRef}"
   }).setMessages(${messages});
   //]]></script>

<div id="${id}" class="document-status document-details-panel">
	<h2 id="${id}-heading" class="thin dark alfresco-twister">
		${msg("header.status")}<span class="panel-body">${status}</span>
	</h2>
</div>
