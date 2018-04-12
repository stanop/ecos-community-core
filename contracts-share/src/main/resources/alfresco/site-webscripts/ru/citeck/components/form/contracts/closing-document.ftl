<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<#if form.mode == "create">
	<@forms.formConfirmSupport createBy=createBy message="Все несохраненные данные будут потеряны" />
</#if>

<@forms.setMandatoryFields
fieldNames = [
"prop_cm_name",
"prop_tk_kind",
"assoc_contracts_closingDocumentAgreement",
"prop_contracts_closingDocumentNumber"
]/>

<@forms.setMandatoryFields fieldNames = [ "prop_cm_content" ] condition="prop_dms_updateContent == 'false'" />

<@forms.fileUploadSupport />

<#assign mode = "" />

<#assign kindRefs = {
"by-agreement": "workspace://SpacesStore/contracts-cat-doctype-contract"
} />

<#if form.mode == "create" && (form.arguments.createBy!) == "by-agreement"
|| (form.data.prop_tk_kind!) == kindRefs["by-agreement"]>
	<#assign mode = "by-agreement" />
</#if>

<script type="text/javascript">//<![CDATA[


<#if form.mode == "create" && mode == "by-agreement" && (page.url.args.contractId!) != "" >
YAHOO.Bubbling.on("objectFinderReady", function (layer, args) {
    var control = args[1].eventGroup;
    if (control.id != "${args.htmlid}_assoc_contracts_closingDocumentAgreement-cntrl") {
        return;
    }
    control.selectItems("${page.url.args.contractId}");
});
</#if>

// auto-fill fields from agreement
YAHOO.Bubbling.on("renderCurrentValue", function (layer, args) {

    var control = args[1].eventGroup;

    if (control.id != "${args.htmlid}_assoc_contracts_closingDocumentAgreement-cntrl") return;

    var currentContracts = control.getSelectedItems();

    if (currentContracts.length == 0) return;

    Alfresco.util.Ajax.jsonGet({
        url: Alfresco.constants.PROXY_URI + "citeck/node?nodeRef=" + currentContracts[0],
        successCallback: {
            fn: function (response) {
                var contractNode = response.json;

                var legalEntityControl = Alfresco.util.ComponentManager.get("${args.htmlid}_assoc_idocs_legalEntity-cntrl");
                if (legalEntityControl && contractNode.assocs['contracts:agreementLegalEntity'] && contractNode.assocs['contracts:agreementLegalEntity'].length > 0) {
                    legalEntityControl.selectItems((contractNode.assocs['contracts:agreementLegalEntity'] || []).join(','));
                }
            }
        }
    });

});

//]]></script>


<#if formUI == "true">
	<@formLib.renderFormsRuntime createBy=createBy />
</#if>

<#if form.mode == "view">
	<#assign twoColumnClass = "yui-g plain" />
	<#assign threeColumnClass = "yui-gb plain" />
<#else>
	<#assign twoColumnClass = "yui-g" />
	<#assign threeColumnClass = "yui-gb" />
</#if>



<@formLib.renderFormContainer createBy=createBy>

	<@forms.renderField field="prop_cm_name" extension = {
	"label": msg("closing-document.form.prop_cm_name.title"),
	"control": {
	"template": "/org/alfresco/components/form/controls/textarea.ftl",
	"params": {
	"rows":"1"
	} } } />

	<#if form.mode == "view">
		<@forms.renderField field="prop_idocs_documentStatus" />
		<@forms.renderField field="prop_cm_created" />
	</#if>


<div class="${threeColumnClass}">
    <div class="yui-u first">
		<@forms.renderField field="assoc_contracts_closingDocumentAgreement" extension = {"control": {
		"template": "/ru/citeck/components/form/controls/association_search.ftl",
		"params": {
		"searchWholeRepo": "true",
		"showTargetLink": "true"
		} } } />
    </div>
    <div class="yui-u">
		<@forms.renderField field="assoc_contracts_closingDocumentPayment" extension = {"control": {
		"template": "/ru/citeck/components/form/controls/association_search.ftl",
		"params": {
		"searchWholeRepo": "true",
		"showTargetLink": "true"
		} } } />
    </div>
    <div class="yui-u">
		<@forms.renderField field="assoc_contracts_closingDocumentOriginalLocation" extension = {"control": {
		"template": "/ru/citeck/components/form/controls/association_search.ftl",
		"params": {
		"searchWholeRepo": "true",
		"showTargetLink": "true"
		}
		}
		} />
    </div>

</div>
<div class="${threeColumnClass}">
    <div class="yui-u first">
		<@forms.renderField field="assoc_contracts_closingDocumentSigner" extension = {
		"endpointType": "cm:person",
		"control": {
		"template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
		"params": {
		} } } />
    </div>
    <div class="yui-u">
		<@forms.renderField field="assoc_idocs_legalEntity" extension = {"control": {
		"template": "/ru/citeck/components/form/controls/association_search.ftl",
		"params": {
		"searchWholeRepo": "true",
		"showTargetLink": "true"
		} } } />
    </div>
    <div class="yui-u">
    </div>
</div>

<div class="${threeColumnClass}">
    <div class="yui-u first">
		<@forms.renderField field="prop_tk_kind" extension = {
		"label" : msg("closing-document.form.prop_tk_kind.title"),
		"control" : {
		"template" : "/org/alfresco/components/form/controls/category.ftl",
		"params": {
		"parentNodeRef": "workspace://SpacesStore/contracts-cat-doctype-closing-doc"
		} } } />
    </div>
    <div class="yui-u">
		<@forms.renderField field="prop_contracts_closingDocumentDate" />
    </div>
    <div class="yui-u">
		<#if form.mode == "create">
        <@forms.renderField field="prop_contracts_closingDocumentNumber" extension = {"control": {
		"template": "/ru/citeck/components/form/controls/auto-manual.ftl",
		"params": {
		} } } />
    <#else>
			<@forms.renderField field="prop_contracts_closingDocumentNumber" />
		</#if>
    </div>
</div>

<script type="text/javascript">// <![CDATA[
Citeck.forms.displayConditional("${args.htmlid}_content", "prop_dms_updateContent == 'false'", ["${args.htmlid}_updateContent"]);
//]]></script>

<div id="${args.htmlid}_updateContent">
    <@forms.renderField field = "prop_dms_updateContent" />
</div>
<div id="${args.htmlid}_content">
	<#if form.mode == "create">
		<@forms.renderField field="prop_cm_content" extension = {
		"label": msg("form.control.file-upload.title.attachment"),
		"control": {
		"template": "/ru/citeck/components/form/controls/fileUpload.ftl"
		} } />
	</#if>
</div>

<#--
    <#if form.mode != "create">
    <div class="${twoColumnClass}">
        <div class="yui-u first">
            <@forms.renderField field="prop_contracts_closingDocumentSentToAccountDep" />
        </div>
        <div class="yui-u">
            <@forms.renderField field="prop_contracts_closingDocumentReceiveAccountDepDate" />
        </div>
    </div>
    </#if>
-->

	<#if form.mode == "create">

    <script type="text/javascript">//<![CDATA[

    window.onload = function() {
//Подписант по умолчанию
        var searchUrl = Alfresco.constants.PROXY_URI + "/citeck/ecosConfig/ecos-config-value?configName=defaultSignatory";
        var request = new XMLHttpRequest();

        request.open('GET', searchUrl, false);  // `false` makes the request synchronous
        request.send(null);
        if (request.status === 200) {
            if (request.responseText) {
                var data = eval('(' + request.responseText + ')');
                if (data.value != "") {
                    var fieldSignatory = "${args.htmlid}_assoc_contracts_closingDocumentSigner-cntrl";
                    var legalSignatoryDesrc = Alfresco.util.ComponentManager.get(fieldSignatory);
                    legalSignatoryDesrc.selectItems(data.value);
                }
            }
        }
    }
    //]]></script>
	</#if>

</@>
