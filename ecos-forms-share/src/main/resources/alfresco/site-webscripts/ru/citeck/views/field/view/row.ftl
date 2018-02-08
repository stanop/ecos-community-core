<#if inlineEdit!false>
	<#assign hideInlineEditButton="true" />
	<@views.renderRegion "label" />
	
	<!-- ko ifnot: inlineEditVisibility -->
	    <#assign defaultTemplate="" />
	    <#list viewScope.field.regions as region>
			<#if region.name == "input" && region.template?? && (region.template?contains("view") || (viewScope.field.params.useViewTemplate?? && viewScope.field.params.useViewTemplate=="true"))>
				<@views.renderElement region />
				<#if region.params.hideInlineEditButton?? && region.params.hideInlineEditButton=="true">
					<#assign hideInlineEditButton="true" />
				<#else>
					<#assign hideInlineEditButton="true" />
				</#if>
    			<#assign defaultTemplate = region.template />
    		</#if>
    	</#list>
        <#if !defaultTemplate?has_content><@views.renderTemplate "view" /></#if>
	<!-- /ko -->

	<!-- ko if: inlineEditVisibility -->
		<@views.renderRegion "input" />
		<div class="region-group region-select-group">
		    <@views.renderRegion "select" />    
		</div>
		<@views.renderRegion "help" />
		<@views.renderRegion "message" />		
	<!-- /ko -->

	<#if hideInlineEditButton=="false">
		<span class="form-field-inline-mode-changer" data-zurab="${hideInlineEditButton}"
			data-bind="click: inlineEditChanger, clickBubble: false, css: { 'save-mode': inlineEditVisibility }">
			<!-- ko if: inlineEditVisibility --><i class="fa fa-floppy-o" aria-hidden="true"></i><!-- /ko -->
			<!-- ko ifnot: inlineEditVisibility --><i class="fa fa-pencil-square-o" aria-hidden="true"></i><!-- /ko -->
		</span>
	</#if>

	<!-- ko ifnot: inlineEditVisibility -->
		<span class="form-field-inline-icons">
			<!-- ko if: invalid -->
				<i class="fa fa-exclamation-circle warning" aria-hidden="true"></i>
			<!-- /ko -->
		</span>
	<!-- /ko -->
<#else>
	<@views.renderRegion "label" />
	<@views.renderRegion "input" />
</#if>