<#if inlineEdit>
	<@views.renderRegion "label" />
	
	<!-- ko ifnot: inlineEditVisibility -->
		<@views.renderTemplate "view" />
	<!-- /ko -->

	<!-- ko if: inlineEditVisibility -->
		<@views.renderRegion "input" />
		<div class="region-group region-select-group">
		    <@views.renderRegion "select" />    
		</div>
		<@views.renderRegion "help" />
		<@views.renderRegion "message" />		
	<!-- /ko -->
<#else>
	<@views.renderRegion "label" />
	<@views.renderRegion "input" />
</#if>