<#macro selectbar field>

<#if !field.control.params.options?? || field.control.params.options == "">
	<#return/>
</#if>

<#-- - - - - - - - - - - - - - - - - - - - - -->
<#-- get optionSeparator and labelSeparator  -->
<#-- - - - - - - - - - - - - - - - - - - - - -->

<#if field.control.params.optionSeparator??>
	<#assign optionSeparator=field.control.params.optionSeparator>
<#else>
	<#assign optionSeparator=",">
</#if>
<#if field.control.params.labelSeparator??>
	<#assign labelSeparator=field.control.params.labelSeparator>
<#else>
	<#assign labelSeparator="|">
</#if>


<#assign items = field.control.params.options?split(optionSeparator) />

<#-- field value - is first option unless it is explicitly set -->
<#assign fieldValue = field.value!"" />
<#if fieldValue == "">
	<#assign fieldValue = items[0]?split(labelSeparator)[0] />
</#if>

<#-- - - - - - - - - - - - - - - - - - - - - -->
<#--        parameters of yui style          -->
<#-- - - - - - - - - - - - - - - - - - - - - -->

<#assign N = items?size />
<#if N < 3 || N == 4>
	<#assign controlclass = "yui-g" />
	<#assign M = 2 />
<#else>
	<#assign controlclass = "yui-gb" />
	<#assign M = 3 />
</#if>

<#if field.control.params.styleClass??>
	<#assign controlclass = controlclass + " " + field.control.params.styleClass />
</#if>


<#assign disabled = !(field.control.params.forceEditable!false) && (field.disabled || form.mode == "view") />

<#-- render control -->

<div id="${fieldHtmlId}-container" class="${controlclass}"
	<#if field.control.params.style??>style="${field.control.params.style}"</#if>
>

	<#assign input_id = fieldHtmlId />

	<#list items as item>

		<#assign parts = item?split(labelSeparator) />
		<#if parts?size &gt; 1 > 
			<#assign value = parts[0] />
			<#assign label = parts[1] />
		<#else>
			<#assign value = item />
			<#assign label = item />
		</#if>

		<div class="yui-u<#if (item_index % M) == 0> first</#if>">

			<input id="${input_id}" type="radio" name="${field.name}" tabindex="0" 
			value="${value?js_string}" <#if value == fieldValue> checked="checked" </#if>
			<#if disabled> disabled="true" </#if>>
			<label for="${input_id}">${label?html}</label>
	
		</div>

		<#assign input_id = fieldHtmlId + '-' + item_index />

	</#list>

</div>

</#macro>

<@selectbar field = field />
