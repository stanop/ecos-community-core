
<#assign controlId = fieldHtmlId + "-cntrl">
<#assign params = field.control.params />

<script type="text/javascript">//<![CDATA[

(function() {
    var selectWithEdit = new Alfresco.SelectWithEditControl("${fieldHtmlId}").setOptions({
        optionsUrl: "${params.optionsUrl}",
        mode: "${form.mode}",
        <#if field.value??>originalValue: "${field.value?js_string}",</#if>
        <#if params.selectedItem??>selectedItem: "${params.selectedItem}",</#if>
        <#if params.responseType??>responseType: ${params.responseType},</#if>
        <#if params.responseSchema??>responseSchema: ${params.responseSchema},</#if>
        <#if params.requestParam??>requestParam: "${params.requestParam}",</#if>
        <#if params.titleField??>titleField: "${params.titleField}",</#if>
        <#if params.valueField??>valueField: "${params.valueField}",</#if>
        <#if params.resultsList??>resultsList: "${params.resultsList}",</#if>
    }).setMessages(${messages});

    Citeck.forms.displayConditional("other-editbox-field", "${field.id}_select == 'other'", ["${fieldHtmlId}-select"]);

})();

//]]></script>

<#assign is_property = field.type == "property" />

<#if form.mode == "view">
    <#assign twoColumnClass = "yui-g plain" />
    <#assign threeColumnClass = "yui-gb plain" />
<#else>
    <#assign twoColumnClass = "yui-g" />
    <#assign threeColumnClass = "yui-gb" />
</#if>

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
        <span class="viewmode-value" id="${fieldHtmlId}"><#if field.value == "">${msg("form.control.novalue")}<#else>${field.value?html}</#if></span>
    </div>
<#else>
    <label for="${fieldHtmlId}-select">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
    <div id="${fieldHtmlId}-error" class="error"></div>
    <div class="${twoColumnClass}">
        <div class="yui-u first">
            <select id="${fieldHtmlId}-select" name="<#if is_property>${field.name}_select<#else>-</#if>" tabindex="0"
                    <#if field.description??>title="${field.description}"</#if>
                    <#if field.control.params.size??>size="${field.control.params.size}"</#if>
                    <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
                    <#if field.control.params.style??>style="${field.control.params.style}"</#if>
                    <#if field.disabled>disabled="true"</#if>>
                <option value="">${selectText?html}</option>
            </select>
            <#--<#assign alreadyModified = params.selectedItem?? && params.selectedItem != field.value />-->
        </div>
        <div class="yui-u">
            <div id="other-editbox-field">
                <input id="${fieldHtmlId}-editbox" name="<#if is_property>${field.name}_editbox<#else>-</#if>" tabindex="1"/>
            </div>
        </div>
        <input type="hidden" id="${fieldHtmlId}" name="<#if is_property>${field.name}<#else>-</#if>" />
    </div>
</#if>
</div>