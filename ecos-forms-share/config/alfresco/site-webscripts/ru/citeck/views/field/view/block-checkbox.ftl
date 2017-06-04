<#if inlineEdit!false>
	<!-- ko if: inlineEditVisibility -->
		<div class="block-region block-region-input">
			<@views.renderRegion "input" />
		</div>
	<!-- /ko -->

	<div class="block-region block-region-label">
		<@views.renderRegion "label" />	

		<!-- ko if: inlineEditVisibility -->
			<@views.renderRegion "help" />
		<!-- /ko -->

		<span class="form-field-inline-mode-changer" 
			data-bind="click: inlineEditChanger, clickBubble: false, css: { 'save-mode': inlineEditVisibility }">
			<!-- ko if: inlineEditVisibility --><i class="fa fa-floppy-o" aria-hidden="true"></i><!-- /ko -->
			<!-- ko ifnot: inlineEditVisibility --><i class="fa fa-pencil-square-o" aria-hidden="true"></i><!-- /ko -->
		</span>
	</div>
	
	<!-- ko ifnot: inlineEditVisibility -->
		<div class="block-region block-region-input">
			<@views.renderTemplate "view" />
		</div>
	<!-- /ko -->

	<@views.renderRegion "message" />
<#else>
	<div class="block-region block-region-label">
		<@views.renderRegion "label" />	
	</div>

	<div class="block-region block-region-input">
		<@views.renderRegion "input" />
	</div>
</#if>