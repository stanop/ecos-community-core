<#assign controlId = fieldHtmlId + "-cntrl">
<#assign params = field.control.params />

<#assign path = params.configPath />
<#assign pathElems = path?split('/') />

<#assign cfg = config.scoped[pathElems[0]] />
<#assign cfg = cfg[pathElems[1]] />
<#assign children = cfg.children />

<#assign valueField = params.valueField! />
<#assign labelField = params.labelField! />

<#if field.value?is_number>
    <#assign fieldValue=field.value?c>
<#else>
    <#assign fieldValue=field.value>
</#if>

<div class="form-field">
<#if form.mode == 'view'>
    <div class="viewmode-field">
        <#if field.mandatory && !(field.value?is_number) && field.value == "">
            <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
        </#if>
        <span class="viewmode-label">${field.label?html}:</span>
        <#if fieldValue == ''>
        <span class="viewmode-value">${msg("form.control.novalue")}</span>
        <#else/>
            <#assign fieldLabel = fieldValue />
            <#list children as child>
                <#if valueField != ''>
                    <#assign childValue = child.attributes[valueField] />
                <#else/>
                    <#assign childValue = child.value />
                </#if>
                <#if fieldValue == childValue>
                    <#if labelField != ''>
                        <#assign fieldLabel = child.attributes[labelField] />
                    <#else/>
                        <#assign fieldLabel = child.value />
                    </#if>
                    <#break />
                </#if>
            </#list>
        <span class="viewmode-value">${msg(fieldLabel)?html}</span>
        </#if>
    </div>
<#else>
    <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
    <select id="${fieldHtmlId}" name="${field.name}">
        <option value="" <#if fieldValue == ''>selected="selected"</#if>>${msg("form.select.label")}</option>
    <#list children as child>
        <#if valueField != ''>
            <#assign childValue = child.attributes[valueField] />
        <#else/>
            <#assign childValue = child.value />
        </#if>
        <#if labelField != ''>
            <#assign childLabel = child.attributes[labelField] />
        <#else/>
            <#assign childLabel = child.value />
        </#if>
        <option value="${childValue}" <#if fieldValue == childValue>selected="selected"</#if>>${msg(childLabel)?html}</option>
    </#list>
    </select>
</#if>
</div>