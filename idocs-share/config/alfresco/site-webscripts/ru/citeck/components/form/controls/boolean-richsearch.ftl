<#assign controlId = fieldHtmlId + "-cntrl">


<script type="text/javascript">//<![CDATA[
(function() {
    new Citeck.BooleanRichSearch("${fieldHtmlId}").setOptions({
        field: "${field.name}",
    <#if field.mandatory??>
        mandatory: ${field.mandatory?string},
    <#elseif field.endpointMandatory??>
        mandatory: ${field.endpointMandatory?string},
    </#if>
        formMode: "${form.mode}",
        defaultMode: "${field.control.params.defaultMode!'any'}"
    }).setMessages(
        ${messages}
    );
})();
//]]></script>

<div class="form-field search-field">
<#if form.mode == "view">
    <div class="viewmode-field">
        <#if field.mandatory && !(field.value?is_number) && field.value == "">
        <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
        </#if>
        <span class="viewmode-label">${field.label?html}:</span>
        <#if field.control.params.activateLinks?? && field.control.params.activateLinks == "true">
            <#assign fieldValue=field.value?html?replace("((http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?\\^=%&:\\/~\\+#]*[\\w\\-\\@?\\^=%&\\/~\\+#])?)", "<a href=\"$1\" target=\"_blank\">$1</a>", "r")>
        <#else>
            <#if field.value?is_number>
                <#assign fieldValue=field.value?c>
            <#else>
                <#assign fieldValue=field.value?html>
            </#if>
        </#if>
        <span class="viewmode-value"><#if fieldValue == "">${msg("form.control.novalue")}<#else>${fieldValue}</#if></span>
    </div>
<#else>
    <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
    <input id="${fieldHtmlId}" name="${field.name}" type="hidden" />
    <input type="button" id="${controlId}-search-mode-button" name="-" />
    <select id="${controlId}-search-mode-select" name="-">
        <option value="yes"> ${msg("button.yes")} </option>
        <option value="no"> ${msg("button.no")} </option>
        <option value="any"> ${msg("button.any")} </option>
    </select>
</#if>
</div>