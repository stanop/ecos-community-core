<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />
<#import "/ru/citeck/components/form/contracts/macros.ftl" as macros />

<#macro renderAgreementCommon type = 'agreement'>

    <#if form.mode == "view">
        <#assign twoColumnClass = "yui-g plain" />
        <#assign threeColumnClass = "yui-gb plain" />
    <#else>
        <#assign twoColumnClass = "yui-g" />
        <#assign threeColumnClass = "yui-gb" />
    </#if>

    <#if type == 'sup-agreement'>
        <#assign typeDocument = 'workspace://SpacesStore/contracts-cat-doctype-supp-agreement' />
    <#else>
        <#assign typeDocument = 'workspace://SpacesStore/contracts-cat-doctype-contract' />
    </#if>

    <@forms.fileUploadSupport />

<div class="${threeColumnClass}">
    <div class="yui-u first">
        <@macros.association fieldName = "assoc_contracts_agreementLegalEntity" endpointType= "idocs:legalEntity" />
    </div>
    <div class="yui-u">
        <@macros.association fieldName = "assoc_contracts_contractor" endpointType= "idocs:contractor" />
    </div>
    <div class="yui-u">
        <@forms.renderField field="prop_contracts_contractWith" />
    </div>
</div>

<div class="${threeColumnClass}">
    <div class="yui-u first">
        <@macros.association fieldName = "assoc_contracts_agreementSubject" endpointType= "contracts:subject" />
    </div>
    <div class="yui-u">
        <@forms.renderField field="prop_tk_kind" extension = {
        "label" : msg("${type}.form.tk_kind.title"),
        "control" : {
        "template" : "/org/alfresco/components/form/controls/category.ftl",
        "params": {
        "parentNodeRef": typeDocument
        }
        }
        } />
    </div>
    <div class="yui-u">
    </div>
</div>

<div class="${threeColumnClass}">
    <div class="yui-u first">
        <#if form.mode == "create">
		<@forms.renderField field="prop_contracts_agreementNumber" extension = {
        "label": msg("${type}.form.contracts_agreementNumber.title"),
        "control": {
        "template": "/ru/citeck/components/form/controls/auto-manual.ftl",
        "params": {
        "style":"width:230px"
        }
        }} />
		<#else>
            <@forms.renderField field="prop_contracts_agreementNumber" extension = {
            "label": msg("${type}.form.contracts_agreementNumber.title")
            }/>
        </#if>
    </div>
    <div class="yui-u">
        <@forms.renderField field="prop_contracts_agreementDate" extension = {
        "label": msg("${type}.form.contracts_agreementDate.title")
        }/>
    </div>
    <div class="yui-u">
        <@forms.renderField field="prop_contracts_duration" />
    </div>
</div>

<div class="${threeColumnClass}">
    <div class="yui-u first">
        <@macros.currency fieldName="assoc_contracts_agreementCurrency" />
    </div>
    <div class="yui-u">
        <@forms.renderField field="prop_contracts_agreementAmount" extension = {
        "label": msg("${type}.form.contracts_agreementAmount.title")
        }/>
    </div>
    <div class="yui-u">
        <@forms.renderField field="prop_contracts_VAT"  />
    </div>
</div>

    <@forms.renderField field="prop_idocs_summary"  extension = {"control": {
    "template": "/org/alfresco/components/form/controls/textarea.ftl",
    "params": {
    "rows":"1"
    }}
    }
    />

<div class="${threeColumnClass}">
    <div class="yui-u first">
        <@forms.renderField field="prop_idocs_appendixPagesNumber" />
    </div>
    <div class="yui-u">
        <@forms.renderField field="prop_idocs_pagesNumber" />
    </div>
    <div class="yui-u">

    </div>
</div>

<div class="${threeColumnClass}">
    <div class="yui-u first">
        <@forms.renderField field="assoc_idocs_signatory" extension = { "control": {
        "template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
        "params": {
        "flatButtonMode": "true"
        }
        }} />
    </div>
    <div class="yui-u">
        <@forms.renderField field="assoc_idocs_performer" extension = { "control": {
        "template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
        "params": {
        "flatButtonMode": "true"
        }
        }} />
    </div>
    <div class="yui-u">

    </div>
</div>

    <@forms.renderField field="prop_idocs_note" extension = extensions.controls.textarea  />

<script type="text/javascript">// <![CDATA[
Citeck.forms.displayConditional("${args.htmlid}_content", "prop_dms_updateContent == 'false'", ["${args.htmlid}_updateContent"]);
//]]></script>

<div id="${args.htmlid}_updateContent">
    <@forms.renderField field = "prop_dms_updateContent" />
</div>

<div id="${args.htmlid}_content">
    <@forms.renderField field="prop_cm_content" extension = {
    "label": msg("form.control.file-upload.title.attachment"),
    "control": {
    "template": "/ru/citeck/components/form/controls/fileUpload.ftl"
    } } />
</div>


    <#if form.mode != "view">
    <div id="${args.htmlid}_payments">
        <@forms.renderField field = "assoc_payments_payments" extension= { "control": {
        "template": "/ru/citeck/components/form/controls/table-children.ftl",
        "params": {
        "columns":'[
{"key": "payments_typePayment", "label": Alfresco.util.message("payment-schedule.payment-type"), formatter: Citeck.format.code({advance:"Аванс", rest:"Остаток"})},
{"key": "payments_plannedPaymentDate", "label": Alfresco.util.message("payment-schedule.date"),  formatter: Citeck.format.datetime("dd.MM.yyyy")},
{"key": "payments_currency_added", "label": Alfresco.util.message("payment-schedule.currency"), formatter: Citeck.format.nodeRef("")},
{"key": "payments_paymentAmount", "label": Alfresco.util.message("payment-schedule.sum")},
{"key": "payments_paymentVAT", "label": Alfresco.util.message("payment-schedule.VAT") },
{"key": "actions", "label": Alfresco.util.message("payment-schedule.actions"), formatter:Citeck.format.actionsNonContent({panelID: "search-criteria",})}
]',
        "responseSchema":
        '{"resultsList": "props",
"fields": [
{"key": "payments_typePayment"},
{"key": "payments_plannedPaymentDate"},
{"key": "payments_currency_added"},
{"key": "payments_paymentAmount"},
{"key": "payments_paymentVAT"},
{"key": "nodeRef"},
]}',
        "destNode": "workspace://SpacesStore/attachments-root",
        "showCancelButton": "true",
        "showAddButton": "true"
        }
        }
        }/>
    </div>
    </#if>

    <#if form.mode == "view">
        <@forms.renderField field="assoc_idocs_signedVersion" extension = { "control": {
        "template": "/ru/citeck/components/form/controls/association_search.ftl",
        "params": {
        "showTargetLink": "true"
        }
        }} />
    </#if>

</#macro>