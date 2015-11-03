<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields
fieldNames = [
"prop_tk_kind",
"prop_cm_name"
]/>

<#if form.mode == "create">
    <@forms.formConfirmSupport formId=formId message="Все несохраненные данные будут потеряны" />
</#if>

<#if formUI == "true">
    <@formLib.renderFormsRuntime formId=formId />
</#if>

<#if form.mode == "view">
    <#assign twoColumnClass = "yui-g plain" />
    <#assign threeColumnClass = "yui-gb plain" />
<#else>
    <#assign twoColumnClass = "yui-g" />
    <#assign threeColumnClass = "yui-gb" />
</#if>

<@forms.fileUploadSupport />

<@formLib.renderFormContainer formId=formId>

    <@forms.renderField field="prop_tk_kind" extension = {
        "label" : "Вид документа",
        "control" : {
        "template" : "/ru/citeck/components/form/controls/types-and-kinds.ftl",
        "params": {
            "rootNode":"workspace://SpacesStore/category-document-type",
            "fixedTypeOption": "workspace://SpacesStore/category-document-type",
            "blank": "kind",
            "mandatory": "kind",
            "hidden":"type",
            "recurse": "true",
            "typeFieldId": "prop_tk_type",
            "kindFieldId": "prop_tk_kind"
        }
        }
        } />

    <@forms.renderField field="prop_cm_name" extension = {
        "label": "Название",
        "control": {
        "template": "/org/alfresco/components/form/controls/textarea.ftl",
        "params": {
        "rows":"1"
        }
        }} />

    <@forms.renderField field="prop_idocs_note" extension = extensions.controls.textarea + {
        "label": "Описание"
    } />

<div class="${twoColumnClass}">
    <div class="yui-u first">
        <@forms.renderField field="prop_ecos_documentDate" />
    </div>
    <div class="yui-u">
        <#if form.mode="create">
        <@forms.renderField field="prop_ecos_documentNumber" extension = { "control": {
        "template": "/ru/citeck/components/form/controls/auto-manual.ftl",
        "params": {
        }
        }} />
    <#else>
        <@forms.renderField field="prop_ecos_documentNumber" />
    </#if>
    </div>
</div>

<#if form.mode !="view">
    <@forms.renderField field="assoc_idocs_legalEntity" extension = {
        "endpointType":"idocs:legalEntity",
        "control": {
        "template": "/ru/citeck/components/form/controls/association_search.ftl",
        "params": {
        "flatButtonMode": "true",
        "searchWholeRepo": "true",
        "evaluateDLDestFolder": "true",
        "showTargetLink": "true"
        }
    }} />

    <@forms.renderField field="assoc_idocs_contractor" extension = {
        "endpointType":"idocs:contractor",
        "control": {
        "template": "/ru/citeck/components/form/controls/association_search.ftl",
        "params": {
        "flatButtonMode": "true",
        "searchWholeRepo": "true",
        "evaluateDLDestFolder": "true",
        "showTargetLink": "true"
        }
    }} />

<#else>
    <@forms.renderField field="assoc_idocs_legalEntity" extension = {
    "control": {
    "template": "/ru/citeck/components/form/controls/association_search.ftl",
    "params": {
    "searchWholeRepo": "true",
    "showTargetLink": "true"
    }
    }} />

    <@forms.renderField field="assoc_idocs_contractor" extension = {
    "control": {
    "template": "/ru/citeck/components/form/controls/association_search.ftl",
    "params": {
    "searchWholeRepo": "true",
    "showTargetLink": "true"
    }
    }} />
</#if>

    <@forms.renderField field="prop_ecos_documentAmount" />

    <@forms.renderField field="prop_ecos_VAT" />

    <@forms.renderField field="assoc_idocs_currencyDocument" extension = { "control": {
            "template": "/ru/citeck/components/form/controls/select.ftl",
            "params": {
            "optionsUrl": "/share/proxy/alfresco/citeck/search/simple?type=idocs:currency&amp;properties=idocs:currencyCode,idocs:currencyName",
            "resultsList":"nodes",
            "valueField":"nodeRef",
            "titleField":"name",
            "style":"width:103px"
            }
    }} />

    <script type="text/javascript">// <![CDATA[
    Citeck.forms.displayConditional("${args.htmlid}_content", "prop_dms_updateContent == 'false'", ["${args.htmlid}_updateContent"]);
    //]]></script>

    <div id="${args.htmlid}_updateContent">
        <@forms.renderField field = "prop_dms_updateContent"  extension = {
        "value":false,
        "control": {
        "template": "/org/alfresco/components/form/controls/checkbox.ftl",
        "params" : {}
        } } />
    </div>

    <div id="${args.htmlid}_content">
        <@forms.renderField field="prop_cm_content" extension = {
        "label": "Вложение",
        "control": {
        "template": "/ru/citeck/components/form/controls/fileUpload.ftl"
        } } />
    </div>

</@>