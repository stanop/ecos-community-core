<div class="form-field">
	<div class="viewmode-field">
		<span class="viewmode-value">
		<#if field.value>
			<img src="${url.context}/res/citeck/images/check.png" style="width: 16px; height: 16px" />
			${field.control.params.trueLabel!}
		<#else>
			<img src="${url.context}/res/citeck/images/warn.png" style="width: 16px; height: 16px" />
			${field.control.params.falseLabel!}
		</#if>
		</span>
	</div>
</div>
