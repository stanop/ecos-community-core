<#include "/org/alfresco/components/form/controls/common/utils.inc.ftl" />
<#assign controlId = fieldHtmlId + "-cntrl">

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

<#if field.control.params.options?? && field.control.params.options != "">
	<#assign options=field.control.params.options>
<#else>
    <#list form.constraints as constraint>
		<#if field.name == constraint.fieldId>
			<#assign allowedValues=(constraint.params?eval).allowedValues/>
			<#if allowedValues?is_sequence>
				<#assign options = ""/>
				<#list allowedValues as value>
					<#assign options=options + value/>
					<#if value_has_next>
						<#assign options=options+","/>
					</#if>
				</#list>
			</#if>
		</#if>
    </#list>
</#if>

<#assign fieldValue=field.value>

<#if fieldValue?string == "" && field.control.params.defaultValueContextProperty??>
    <#if context.properties[field.control.params.defaultValueContextProperty]??>
        <#assign fieldValue = context.properties[field.control.params.defaultValueContextProperty]>
    <#elseif args[field.control.params.defaultValueContextProperty]??>
        <#assign fieldValue = args[field.control.params.defaultValueContextProperty]>
    </#if>
</#if>

<#if form.mode == "view">
	<#assign selectText = msg("form.control.novalue") />
<#elseif field.control.params.selectText??>
	<#assign selectText = field.control.params.selectText />
<#else>
	<#assign selectText = msg("form.select.label") />
</#if>

<#if field.control.params.optionsUrl??>
	<#assign optionsUrl = field.control.params.optionsUrl />
<#else />
	<#assign optionsUrl = "" />
</#if>

<script type="text/javascript">//<![CDATA[
(function() {
    new Citeck.SelectRichSearch("${fieldHtmlId}").setOptions({
        field: "${field.name}",
    <#if field.mandatory??>
        mandatory: ${field.mandatory?string},
    <#elseif field.endpointMandatory??>
        mandatory: ${field.endpointMandatory?string},
    </#if>
    <#if field.control.params.valueType??>
        valueType: "${field.control.params.valueType}",
    </#if>

        selectActionLabel: "${field.control.params.selectActionLabel!msg("button.select")}",
        currentValue: "${fieldValue}",
        formMode: "${form.mode}",
        defaultMode: "${field.control.params.defaultMode!'any'}"
    }).setMessages(
        ${messages}
    );
    
    var optionsUrlValue = "${optionsUrl}";
    if(optionsUrlValue!="")
    {
        var select = new Alfresco.SelectControl("${fieldHtmlId}-value-selectbox").setOptions({
            <#if field.control.params.optionsUrl??>optionsUrl: "${field.control.params.optionsUrl}",</#if>
            mode: "${form.mode}",
            <#if field.value??>originalValue: "${field.value?js_string}",</#if>
            <#if field.control.params.selectedItem??>selectedItem: "${field.control.params.selectedItem}",</#if>
            <#if field.control.params.responseType??>responseType: ${field.control.params.responseType},</#if>
            <#if field.control.params.responseSchema??>responseSchema: ${field.control.params.responseSchema},</#if>
            <#if field.control.params.requestParam??>requestParam: "${field.control.params.requestParam}",</#if>
            <#if field.control.params.titleField??>titleField: "${field.control.params.titleField}",</#if>
            <#if field.control.params.valueField??>valueField: "${field.control.params.valueField}",</#if>
            <#if field.control.params.resultsList??>resultsList: "${field.control.params.resultsList}",</#if>
        }).setMessages(${messages});
    }

})();
//]]></script>

<#assign is_property = field.type == "property" />

<div class="form-field search-field">
<#if form.mode == "view">
    <div class="viewmode-field">
        <#if field.mandatory && !(fieldValue?is_number) && fieldValue?string == "">
        <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
        </#if>
        <span class="viewmode-label">${field.label?html}:</span>
        <div id="${fieldHtmlId}-error" class="error"></div>
        <span class="viewmode-value" id="${fieldHtmlId}"><#if field.value == "">${msg("form.control.novalue")}<#else>${field.value?html}</#if></span>
        <span class="viewmode-label">${field.label?html}:</span>
        <#if fieldValue?string == "">
            <#assign valueToShow=msg("form.control.novalue")>
        <#else>
            <#assign valueToShow=fieldValue>
            <#if options?? && options != "">
                <#list options?split(optionSeparator) as nameValue>
                    <#if nameValue?index_of(labelSeparator) == -1>
                        <#if nameValue == fieldValue?string || (fieldValue?is_number && fieldValue?c == nameValue)>
                            <#assign valueToShow=nameValue>
                            <#break>
                        </#if>
                    <#else>
                        <#assign choice=nameValue?split(labelSeparator)>
                        <#if choice[0] == fieldValue?string || (fieldValue?is_number && fieldValue?c == choice[0])>
                            <#assign valueToShow=msgValue(choice[1])>
                            <#break>
                        </#if>
                    </#if>
                </#list>
            </#if>
        </#if>
        <span class="viewmode-value">${valueToShow?html}</span>
    </div>
<#else>
    <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
        <table>
            <tr>
                <td>
                    <input id="${fieldHtmlId}" name="${field.name}" type="hidden" />
                    <input type="button" id="${controlId}-search-mode-button" name="-" />
                    <select id="${controlId}-search-mode-select" name="-">
                        <option value="equal"> ${msg("button.searchValue")} </option>
                        <option value="notequal"> ${msg("button.searchExeptValue")} </option>
                        <option value="empty"> ${msg("button.searchEmpty")} </option>
                        <option value="not_empty"> ${msg("button.searchNotEmpty")} </option>
                        <option value="any"> ${msg("button.any")} </option>
                    </select>
                </td>
    <#if field.control.params.optionsUrl??>
                <td>
                    <select id="${fieldHtmlId}-value-selectbox" name="<#if is_property>${field.name}<#else>-</#if>" tabindex="0" 
                        <#if field.description??>title="${field.description}"</#if>
                        <#if field.control.params.size??>size="${field.control.params.size}"</#if> 
                        <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
                        <#if field.control.params.style??>style="${field.control.params.style}"</#if>
                        <#if field.disabled>disabled="true"</#if>>
                        <option value="">${selectText?html}</option>
                    </select>
                    <#assign alreadyModified = field.control.params.selectedItem?? && field.control.params.selectedItem != field.value />
                    <input type="hidden" id="${fieldHtmlId}-added" name="<#if !is_property>${field.name}_added<#else>-</#if>" <#if alreadyModified>value="${field.control.params.selectedItem?html}"</#if> />
                    <input type="hidden" id="${fieldHtmlId}-removed" name="<#if !is_property>${field.name}_removed<#else>-</#if>" <#if alreadyModified>value="${field.value?html}"</#if> />
                </td>
                </tr>
            </table>
    <#else>
        <#if options?? && options != "">
                    <td>
                        <select id="${fieldHtmlId}-value-selectbox" name="-">
                            <#list options?split(optionSeparator) as nameValue>
                                <#if nameValue?index_of(labelSeparator) == -1>
                                    <option value="${nameValue?html}"<#if nameValue == fieldValue?string || (fieldValue?is_number && fieldValue?c == nameValue)> selected="selected"</#if>>${nameValue?html}</option>
                                <#else>
                                    <#assign choice=nameValue?split(labelSeparator)>
                                    <option value="${choice[0]?html}"<#if choice[0] == fieldValue?string || (fieldValue?is_number && fieldValue?c == choice[0])> selected="selected"</#if>>${msgValue(choice[1])?html}</option>
                                </#if>
                            </#list>
                        </select>
                    </td>
                </tr>
            </table>
        <#else>
            <div id="${fieldHtmlId}" class="missing-options">${msg("form.control.selectone.missing-options")}</div>
        </#if>
    </#if>
</#if>
</div>