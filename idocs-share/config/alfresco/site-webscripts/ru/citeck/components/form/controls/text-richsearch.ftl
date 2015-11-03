<#assign controlId = fieldHtmlId + "-cntrl">

<script type="text/javascript">//<![CDATA[
    (function() {
        var textRichSearch = new Citeck.TextRichSearch("${fieldHtmlId}").setOptions({
            multipleSelectMode: ${field.endpointMany?string},
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
            currentValue: "${field.value}",
            formMode: "${form.mode}",
            defaultMode: "${field.control.params.defaultMode!'match_string'}",
            hideSelect: "${field.control.params.hideSelect!'false'}"
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
      <input id="${fieldHtmlId}" name="${field.name}" tabindex="0"
             <#if field.control.params.password??>type="password"<#else>type="text"</#if>
             <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
             <#if field.control.params.style??>style="${field.control.params.style}"</#if>
             <#if field.description??>title="${field.description}"</#if>
             <#if field.control.params.maxLength??>maxlength="${field.control.params.maxLength}"</#if> 
             <#if field.control.params.size??>size="${field.control.params.size}"</#if> 
             <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if> />
       <input type="button" id="${controlId}-search-mode-button" name="-" />
       <select id="${controlId}-search-mode-select" name="-">
           <option value="match_string"> ${msg("button.searchContains")} </option>
           <option value="empty"> ${msg("button.searchEmpty")} </option>
           <option value="not_empty"> ${msg("button.searchNotEmpty")} </option>
           <option value="equal"> ${msg("button.searchEquals")} </option>
           <option value="not_equal"> ${msg("button.searchNotEquals")} </option>
       </select>
      <@formLib.renderFieldHelp field=field />
   </#if>
</div>