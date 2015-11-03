<#--
	control for "number" fields with "Generate" button.
	It sends request to auto-number webscript to generate new number
-->

<#if form.mode == "view">
<#include "/org/alfresco/components/form/controls/textfield.ftl" />
<#else />
<div class="form-field auto-number-field">
      <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      <input id="${fieldHtmlId}" name="${field.name}" tabindex="0"
             <#if field.control.params.styleClass??>class="generated-field ${field.control.params.styleClass}"</#if>
             <#if field.control.params.style??>style="${field.control.params.style}"</#if>
             <#if field.value?is_number>value="${field.value?c}"<#else>value="${field.value?html}"</#if>
             <#if field.description??>title="${field.description}"</#if>
             <#if field.control.params.maxLength??>maxlength="${field.control.params.maxLength}"</#if> 
             <#if field.control.params.size??>size="${field.control.params.size}"</#if> 
             <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if> />
	<#-- in non-view mode it has "Generate" button -->
	<script type="text/javascript">//<![CDATA[
		new Citeck.forms.AutoNumberControl("${fieldHtmlId}").setOptions({
			template: "${field.control.params.autonumberTemplate}",
			itemType: "${args.itemKind}",
			itemId: "${args.itemId}",
			errors: [
				{
					regexp: '"message"\\s*[:]\\s*"org.alfresco.repo.forms.FormException[:] [\\d]+ (.*[^\\\\])"',
					message: "{1}"
				},
				{
					regexp: '"message"\\s*[:]\\s*"(.*[^\\\\])"',
					message: "{1}"
				}
			]
		});
	//]]></script>
</div>
</#if>