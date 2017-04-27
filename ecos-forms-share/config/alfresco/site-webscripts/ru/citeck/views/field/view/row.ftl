<#if inlineEdit>
	<@views.renderRegion "label" />
	<@views.renderRegion "mandatory" />
	
	<div class="form-region-view-mode">
		<@views.renderTemplate "view" />
	</div>

	<div class="form-region-edit-mode">
		<@views.renderRegion "input" />
		<div class="region-group region-select-group">
		    <@views.renderRegion "select" />    
		</div>
		<@views.renderRegion "help" />
		<@views.renderRegion "message" />		
	</div>
<#else>
	<@views.renderRegion "label" />
	<@views.renderRegion "input" />
</#if>