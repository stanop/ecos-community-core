<#import "common/helper.lib.ftl" as helper />
<#assign controlId = fieldHtmlId + "-cntrl">


<div class="form-field">
<#if form.mode == "view">
    <div class="viewmode-field">
        <#if field.mandatory && !(field.value?is_number) && field.value == "">
        <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
        </#if>
        <span class="viewmode-label">${field.label?html}:</span>
        <#if field.control.params.activateLinks?? && field.control.params.activateLinks == "true">
            <#assign fieldValue=field.value?html?replace("((http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?\\^=%&:\\/~\\+#]*[\\w\\-\\@?\\^=%&\\/~\\+#])?)", "<a href=\"$1\" target=\"_blank\">$1</a>", "r")>
        <#else>
			<#assign fieldValue = helper.getFieldValue(field) />
        </#if>
        <span class="viewmode-value"><#if fieldValue == "">${msg("form.control.novalue")}<#else>${fieldValue}</#if></span>
    </div>
<#else>
    <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
    <input type="text" value="${field.control.params.finalValueLabel}" disabled/>
    <input id="${fieldHtmlId}" name="${field.name}" tabindex="0" type="hidden" value="${field.control.params.finalValue}"/>
</#if>
</div>