<#assign controlId = fieldHtmlId + "-cntrl">

<script type="text/javascript">//<![CDATA[
    (function() {
        var numberRichSearch = new Citeck.NumberRichSearch("${fieldHtmlId}").setOptions({
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
            defaultMode: "${field.control.params.defaultMode!'equal'}"
        }).setMessages(
            ${messages}
        );
    })();
//]]></script>


<div class="form-field search-field">

   <#if form.mode == "view"> <#-- // deprecated -->
       <div class="viewmode-field">
           <#if field.mandatory && !(field.value?is_number) && field.value == "">
           <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
           </#if>
           <span class="viewmode-label">${field.label?html}:</span>
           <span class="viewmode-value"><#if field.value?is_number>${field.value?c}<#elseif field.value == "">${msg("form.control.novalue")}<#else>${field.value?html}</#if></span>
       </div>
   <#else>
       <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
       <input id="${fieldHtmlId}" type="text" name="${field.name}" tabindex="0"
              class="number<#if field.control.params.styleClass??> ${field.control.params.styleClass}</#if>"
              <#if field.control.params.style??>style="${field.control.params.style}"</#if>
              <#if field.description??>title="${field.description}"</#if>
              <#if field.control.params.maxLength??>maxlength="${field.control.params.maxLength}"</#if>
              <#if field.control.params.size??>size="${field.control.params.size}"</#if>
              <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if> />
       <input type="button" id="${controlId}-search-mode-button" name="-" />
       <select id="${controlId}-search-mode-select" name="-">
           <option value="equal"> = ${msg("button.searchEqual")} </option>
           <option value="not_equal"> != ${msg("button.searchNotEqual")} </option>
           <option value="more_than_exclusive"> > ${msg("button.searchMoreThanExclusive")} </option>
           <option value="more_than_inclusive"> >= ${msg("button.searchMoreThanInclusive")} </option>
           <option value="less_than_exclusive"> < ${msg("button.searchLessThanExclusive")} </option>
           <option value="less_than_inclusive"> <= ${msg("button.searchLessThanInclusive")} </option>
           <option value="empty"> ${msg("button.searchEmpty")} </option>
           <option value="not_empty"> ${msg("button.searchNotEmpty")} </option>
       </select>
       <@formLib.renderFieldHelp field=field />
   </#if>
   
</div>