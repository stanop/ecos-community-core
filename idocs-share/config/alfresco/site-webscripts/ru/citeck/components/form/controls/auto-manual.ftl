<div class="form-field ${field.control.params.styleClass!""}"
	<#if field.control.params.style??>style="${field.control.params.style}"</#if>
>
	<#if form.mode == "view">
		<div class="viewmode-field">
			<span class="viewmode-label">${field.label?html}:</span>
			<span class="viewmode-value">${field.value?html}</span>
		</div>
	<#else>
		<input id="${fieldHtmlId}" name="${field.name}" type="hidden" value="${field.value}" />

		<label for="${fieldHtmlId}-input">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
		<input id="${fieldHtmlId}-input" name="-" type="text" class="auto-manual-input" value="${field.value}" />

		<div class="auto-manual-select">
			<input id="${fieldHtmlId}-auto" name="-" type="checkbox" 
				<#if field.control.params.auto!true>checked="checked"</#if> />
			<label for="${fieldHtmlId}-auto">${msg("button.generate")}</label>
		</div>

		<script>
			new Citeck.widget.AutoManualInput("${fieldHtmlId}");
		</script>
	</#if>
</div>
