<#if inlineEdit>
	<!-- ko ifnot: inlineEditVisibility -->
		<@views.renderRegion "label" />
		<@views.renderTemplate "view" />
	<!-- /ko -->

	<!-- ko if: inlineEditVisibility -->
		<@views.renderRegion "input" />
		<@views.renderRegion "label" />
		<@views.renderRegion "help" />
		<@views.renderRegion "message" />		
	<!-- /ko -->
<#else>
	<@views.renderRegion "input" />
	<@views.renderRegion "label" />
</#if>