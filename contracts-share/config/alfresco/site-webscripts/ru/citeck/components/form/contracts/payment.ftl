<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />
<#import "/ru/citeck/components/form/contracts/macros.ftl" as macros />

<#assign mode = "" />
<#assign mode = "" />
<#assign kindRefs = {
"by-agreement": "workspace://SpacesStore/contracts-cat-doctype-contract"
} />

<@forms.setMandatoryFields
fieldNames = [
"assoc_payments_payer",
"assoc_payments_beneficiary",
"prop_payments_paymentNumber"
]/>

<@forms.setMandatoryFields fieldNames = [ "prop_cm_content" ] condition="prop_dms_updateContent == 'false'" />

<#if form.mode == "create" && (form.arguments.formId!) == "by-agreement"
|| (form.data.prop_tk_kind!) == kindRefs["by-agreement"]>
	<#assign mode = "by-agreement" />
</#if>

<#if form.mode == "create">
	<@forms.formConfirmSupport formId=formId message="Все несохраненные данные будут потеряны" />
</#if>

<@forms.fileUploadSupport />

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

<script type="text/javascript">//<![CDATA[

<#-- load values from agreement-->
<#if form.mode == "create" && mode == "by-agreement" && (page.url.args.contractId!) != "" >
YAHOO.Bubbling.on("objectFinderReady", function(layer, args) {
    var control = args[1].eventGroup;
    if(control.id != "${args.htmlid}_assoc_payments_basis-cntrl" ) {
        return;
    }
    control.selectItems("${page.url.args.contractId}");
});
</#if>

<#if form.mode == "create">

function onChangePaymentFor() {
    var controlPaymentFor = document.getElementById("${args.htmlid}_prop_payments_paymentFor");
    var paymentNumberControl = Alfresco.util.ComponentManager.get("${args.htmlid}_prop_payments_paymentNumber");
    if (controlPaymentFor && paymentNumberControl) {
        if (controlPaymentFor.value == 'client') {
            if (!paymentNumberControl.widgets.auto.checked) {
                $("input#${args.htmlid}_prop_payments_paymentNumber-auto").trigger('click');

            }
        }else{
                if (paymentNumberControl.widgets.auto.checked) {
                    $("input#${args.htmlid}_prop_payments_paymentNumber-auto").trigger('click');
                }
        }
    }
}

$(document).ready(function(){
    $("select#${args.htmlid}_prop_payments_paymentFor").change(onChangePaymentFor);
});

// auto-fill fields from contract
YAHOO.Bubbling.on("renderCurrentValue", function(layer, args) {

    var control = args[1].eventGroup;

    if(control.id != "${args.htmlid}_assoc_payments_basis-cntrl") return;

    var currentContracts = control.getSelectedItems();
    //var currentContracts = control.eventGroup.selectedItems;
    if(currentContracts.length == 0) return;

    Alfresco.util.Ajax.jsonGet({
        url: Alfresco.constants.PROXY_URI + "citeck/node?nodeRef="+currentContracts[0],
        successCallback: {
            fn: function (response) {
                var contractNode = response.json;

                //paymentFor
                var controlPaymentFor = document.getElementById("${args.htmlid}_prop_payments_paymentFor");

                if (controlPaymentFor && contractNode.props['contracts:contractWith']) {
                    if (contractNode.props['contracts:contractWith'] ==='client'){
                        controlPaymentFor.value = contractNode.props['contracts:contractWith'];
                    }else{
                         if (contractNode.props['contracts:contractWith'] ==='performer'){
                             controlPaymentFor.value = contractNode.props['contracts:contractWith'];
                         }
                    }
                    onChangePaymentFor();
                }

                //получатель
                var contractorControl = Alfresco.util.ComponentManager.get("${args.htmlid}_assoc_payments_beneficiary-cntrl");
                if (contractorControl && contractNode.assocs['contracts:contractor'] && contractNode.assocs['contracts:contractor'].length>0) {
                    contractorControl.selectItems((contractNode.assocs['contracts:contractor'] || []).join(','));
                }
                //плательщик
                var payerControl = Alfresco.util.ComponentManager.get("${args.htmlid}_assoc_payments_payer-cntrl");
                if (payerControl && contractNode.assocs['contracts:agreementLegalEntity'] && contractNode.assocs['contracts:agreementLegalEntity'].length>0) {
                    payerControl.selectItems((contractNode.assocs['contracts:agreementLegalEntity'] || []).join(','));
                }
                //плановая дата оплаты
                var dateControl = Alfresco.util.ComponentManager.get("${args.htmlid}_prop_payments_plannedPaymentDate-cntrl");
                if (dateControl && contractNode.props['contracts:agreementDate']) {

                    var paymentDate = YAHOO.util.Dom.get(dateControl.currentValueHtmlId);
                    paymentDate.value = contractNode.props['contracts:agreementDate'];
                    if (paymentDate && paymentDate.value) {
                        var paymentDateTime = Alfresco.util.fromISO8601(paymentDate.value);
                        if (paymentDateTime) {
                            var newDate = new Date(Date.UTC(
                                    paymentDateTime.getFullYear(),
                                    paymentDateTime.getMonth(),
                                    paymentDateTime.getDate(),
                                    0, 0, 0
                            ));
                            dateControl.setOptions({currentValue: Alfresco.util.toISO8601(newDate)});
                            dateControl.onReady();
                        }
                    }
                }

                //ндс
                var vatControl = document.getElementById("${args.htmlid}_prop_payments_paymentVAT");
                if (vatControl && contractNode.props['contracts:VAT']) {
                    vatControl.value = contractNode.props['contracts:VAT'];
                }

                //валюта
                var currencyControl = Alfresco.util.ComponentManager.get("${args.htmlid}_assoc_payments_currency");
                if (currencyControl && contractNode.assocs['contracts:agreementCurrency'] && contractNode.assocs['contracts:agreementCurrency'].length>0) {
                    currencyControl.selectItem(contractNode.assocs['contracts:agreementCurrency'][0]);
                }
            }
        }
    });
});
</#if>
//]]></script>


<@formLib.renderFormContainer formId=formId>

<#if form.mode == "view">
<div class="${threeColumnClass}">
	<div class="yui-u first">
<@forms.renderField field="prop_idocs_documentStatus" />
	</div>
	<div class="yui-u">
<@forms.renderField field="prop_payments_exportedToERP" />
	</div>
    <div class="yui-u">
        <@forms.renderField field="prop_cm_created" />
    </div>
</div>
</#if>
<!-- указать endpoinType, чтобы находил только договоры-->
<div class="${threeColumnClass}">
	<div class="yui-u first">
		<@forms.renderField field="assoc_payments_basis" extension = { "control": {
        "endpointType":"",
		"template": "/ru/citeck/components/form/controls/association_search.ftl",
		"params": {
			"searchWholeRepo": "true",
			"showTargetLink": "true"
		}
		}} />
	</div>
	<div class="yui-u">
        <@macros.association fieldName = "assoc_payments_budgetItem" endpointType= "budget:item" />
	</div>
    <div class="yui-u">
            <@forms.renderField field="prop_payments_paymentFor"/>
    </div>
</div>
<@forms.renderField field="prop_payments_paymentPurpose" extension = extensions.controls.textarea />

<div class="${threeColumnClass}">
	<div class="yui-u first">
       <@macros.association fieldName = "assoc_payments_payer" endpointType= "idocs:legalEntity" />
	</div>
	<div class="yui-u">
        <@forms.renderField field="prop_payments_paymentArrangement" />
	</div>
    <div class="yui-u">
        <#if form.mode="create">
         <@forms.renderField field="prop_payments_paymentNumber" extension = { "control": {
        "template": "/ru/citeck/components/form/controls/auto-manual.ftl",
        "params": {
        }
        }} />
        <#else>
            <@forms.renderField field="prop_payments_paymentNumber" />
        </#if>
    </div>
</div>

<div class="${threeColumnClass}">
	<div class="yui-u first">
        <@macros.association fieldName = "assoc_payments_beneficiary" endpointType= "idocs:contractor" />
	</div>
	<#if form.mode!="create">
	<div class="yui-u">
        <@macros.association fieldName = "assoc_payments_beneficiaryAccount" endpointType= "idocs:bankAccount" />
    </div>
	</#if>
	<div class="yui-u">
<@forms.renderField field="prop_payments_beneficiaryType" />
	</div>
</div>

<div class="${threeColumnClass}">
	<div class="yui-u first">

		<@forms.renderField field="prop_payments_plannedPaymentDate" />
	</div>
	<div class="yui-u">
<@forms.renderField field="assoc_payments_period" extension = { "control": {
        "template": "/ru/citeck/components/form/controls/select.ftl",
        "params": {
        "optionsUrl": "/share/proxy/alfresco/citeck/search/simple?type=payments:period",
        "resultsList": "nodes",
        "valueField": "nodeRef",
        "titleField": "name",
        "style": "width:230px"
}
}} />
	</div>
	<div class="yui-u">
		<#if form.mode!="create">
			<@forms.renderField field="prop_payments_paymentDate" />
		</#if>
	</div>
</div>

<div class="${threeColumnClass}">
	<div class="yui-u first">
        <@macros.currency fieldName="assoc_payments_currency" />
	</div>
	<div class="yui-u">
<@forms.renderField field="prop_payments_paymentAmount" />
	</div>
	<div class="yui-u">
<@forms.renderField field="prop_payments_paymentVAT" />
	</div>
</div>

<div class="${threeColumnClass}">
	<div class="yui-u first">
		<@forms.renderField field="prop_payments_billDate" />
	</div>
</div>

<@forms.renderField field="prop_payments_overexpenditure" />

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

<@forms.renderField field="prop_payments_paymentComment" extension = extensions.controls.textarea />

</@>
