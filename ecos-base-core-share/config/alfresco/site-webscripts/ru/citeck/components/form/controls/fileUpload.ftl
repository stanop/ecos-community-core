<#-- 
IMPORTANT NOTICE: 
to use this control, form.enctype should be set to "multipart/form-data".
So if you use custom form template, you write in the beginning:
	<#global form = form + {
		"enctype": "multipart/form-data"
	} />
If you do not use custom form template, you can use "multipart-form.ftl" as a template:
	<create-form template="/ru/citeck/templates/multipart-form.ftl" />
-->

<#if form.mode != "view">
<div class="form-field">
    <label for="${fieldHtmlId}">${msg(field.label)}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
    <input type="file" id="${fieldHtmlId}"
		<#if field.control.params?? && field.control.params.mimeType??> accept="${field.control.params.mimeType}"</#if>
		   name="${field.name}" style="background-color:inherit;" />
    <#if field.mandatory>
    <script type="text/javascript">//<![CDATA[
    YAHOO.util.Event.on("${fieldHtmlId}", "change", function() { 
        YAHOO.Bubbling.fire("mandatoryControlValueUpdated") 
    });
    //]]></script>
    </#if>
</div>
<#else/>
<div class="viewmode-field">
	<span class="viewmode-label">${field.label?html}:</span>
	<#if field.value?? && field.value != "">
		<#assign nodeRef = args.itemId />
		<#assign downloadUrl = url.context + "/proxy/alfresco/api/node/content/" + nodeRef?replace(":/","") + "?a=true" />
		<span class="viewmode-value"><a href="${downloadUrl?html}">${msg("label.download")}</a></span>
		<!-- ${field.value} -->
	<#else/>
		<#if field.mandatory>
			<span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
		</#if>
		<span class="viewmode-value">${msg("label.none")}</span>
	</#if>
</div>
</#if>
