<#macro selectFieldHTML fieldHtmlId field>
    <#assign is_property = field.type == "property" />

    <#if form.mode == "view">
        <#assign selectText = msg("form.control.novalue") />
    <#elseif field.control.params.selectText??>
        <#assign selectText = field.control.params.selectText />
    <#else>
        <#assign selectText = msg("form.select.label") />
    </#if>

    <div class="form-field select-control">
        <#if form.mode == "view">
            <div class="viewmode-field">
                <#if field.mandatory && !(field.value?is_number) && field.value == "">
                    <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
                </#if>
                <span class="viewmode-label">${field.label?html}:</span>
                <div id="${fieldHtmlId}-error" class="error"></div>
                <span class="viewmode-value" id="${fieldHtmlId}"><#if field.value == "">${msg("form.control.novalue")}<#else>${msg('label.loading')}</#if></span>
            </div>
        <#else>
            <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
            <div id="${fieldHtmlId}-error" class="error"></div>
            <select id="${fieldHtmlId}" name="<#if is_property>${field.name}<#else>-</#if>" tabindex="0" 
                <#if field.control.params.multiple??>multiple="${field.control.params.multiple}"</#if> 
                <#if field.description??>title="${field.description}"</#if>
                <#if field.control.params.size??>size="${field.control.params.size}"</#if> 
                <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
                <#if field.control.params.style??>style="${field.control.params.style}"</#if>
                <#if field.disabled>disabled="true"</#if>>
                <option value="">${selectText?html}</option>
            </select>
            <#assign alreadyModified = params.selectedItem?? && params.selectedItem != field.value />
            <input type="hidden" id="${fieldHtmlId}-added" name="<#if !is_property>${field.name}_added<#else>-</#if>" <#if alreadyModified>value="${params.selectedItem?html}"</#if> />
            <input type="hidden" id="${fieldHtmlId}-removed" name="<#if !is_property>${field.name}_removed<#else>-</#if>" <#if alreadyModified>value="${field.value?html}"</#if> />
        </#if>
    </div>
</#macro>
