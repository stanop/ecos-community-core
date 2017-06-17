<#if inlineEdit!false>
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

	<span class="form-field-inline-mode-changer" 
		data-bind="click: inlineEditChanger, clickBubble: false, css: { 'save-mode': inlineEditVisibility }">
		<!-- ko if: inlineEditVisibility --><i class="fa fa-floppy-o" aria-hidden="true"></i><!-- /ko -->
		<!-- ko ifnot: inlineEditVisibility --><i class="fa fa-pencil-square-o" aria-hidden="true"></i><!-- /ko -->
	</span>
<#else>
	<@views.renderRegion "label" />
	<@views.renderRegion "input" />
</#if>