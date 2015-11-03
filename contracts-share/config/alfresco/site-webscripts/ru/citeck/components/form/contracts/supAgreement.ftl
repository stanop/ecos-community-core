<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />
<#import "/ru/citeck/components/form/contracts/agreement-common.ftl" as agreementCommon />

<#if form.mode == "create">
    <@forms.formConfirmSupport formId=formId message="Все несохраненные данные будут потеряны" />
</#if>

<@forms.setMandatoryFields
fieldNames = [
"assoc_contracts_agreementLegalEntity",
"assoc_contracts_contractor",
"assoc_contracts_mainAgreement",
"prop_contracts_agreementNumber"
]/>
<@forms.fileUploadSupport />

<@forms.setMandatoryFields fieldNames = [ "prop_cm_content" ] condition="prop_dms_updateContent == 'false'" />

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

<#assign mode = "" />

<#assign kindRefs = {
"by-agreement": "workspace://SpacesStore/contracts-cat-doctype-contract"
} />

<#if form.mode == "create" && (form.arguments.formId!) == "by-agreement"
|| (form.data.prop_tk_kind!) == kindRefs["by-agreement"]>
    <#assign mode = "by-agreement" />
</#if>

<script type="text/javascript">//<![CDATA[

<#if form.mode == "create">
<#-- load values from agreement-->
    <#if form.mode == "create" && mode == "by-agreement" && (page.url.args.contractId!) != "" >
    YAHOO.Bubbling.on("objectFinderReady", function (layer, args) {
        var control = args[1].eventGroup;
        if (control.id != "${args.htmlid}_assoc_contracts_mainAgreement-cntrl") {
            return;
        }
        control.selectItems("${page.url.args.contractId}");
    });
    </#if>

// auto-fill fields from agreement
YAHOO.Bubbling.on("renderCurrentValue", function (layer, args) {

    var control = args[1].eventGroup;

    if (control.id != "${args.htmlid}_assoc_contracts_mainAgreement-cntrl") return;

    var currentContracts = control.getSelectedItems();
    //var currentContracts = control.eventGroup.selectedItems;
    if (currentContracts.length == 0) return;

    Alfresco.util.Ajax.jsonGet({
        url: Alfresco.constants.PROXY_URI + "citeck/node?nodeRef=" + currentContracts[0],
        successCallback: {
            fn: function (response) {
                var contractNode = response.json;
                //contractor
                var contractorControl = Alfresco.util.ComponentManager.get("${args.htmlid}_assoc_contracts_contractor-cntrl");
                if (contractorControl && contractNode.assocs['contracts:contractor'] && contractNode.assocs['contracts:contractor'].length > 0) {
                    contractorControl.selectItems((contractNode.assocs['contracts:contractor'] || []).join(','));
                }
                //legal entity
                var legalEntityControl = Alfresco.util.ComponentManager.get("${args.htmlid}_assoc_contracts_agreementLegalEntity-cntrl");
                if (legalEntityControl && contractNode.assocs['contracts:agreementLegalEntity'] && contractNode.assocs['contracts:agreementLegalEntity'].length > 0) {
                    legalEntityControl.selectItems((contractNode.assocs['contracts:agreementLegalEntity'] || []).join(','));
                }
                // subject of agreement
                var subjectControl = Alfresco.util.ComponentManager.get("${args.htmlid}_assoc_contracts_agreementSubject-cntrl");
                if (subjectControl && contractNode.assocs['contracts:agreementSubject'] && contractNode.assocs['contracts:agreementSubject'].length > 0) {
                    subjectControl.selectItems((contractNode.assocs['contracts:agreementSubject'] || []).join(','));
                }
                // kind of agreement
                /*var kindDocumentControl = Alfresco.util.ComponentManager.get("${args.htmlid}_prop_tk_kind-cntrl");
                if (kindDocumentControl && contractNode.props['tk:kind'].nodeRef) {
                    kindDocumentControl.selectItems((contractNode.props['tk:kind'].nodeRef));
                }*/
            }
        }
    });
});
</#if>
//]]></script>


<@formLib.renderFormContainer formId=formId>

<div class="${twoColumnClass}">
    <div class="yui-u first">
        <@forms.renderField field="assoc_contracts_mainAgreement" extension = { "control": {
        "template": "/ru/citeck/components/form/controls/association_search.ftl",
        "params": {
        "flatButtonMode": "true",
        "searchWholeRepo": "true",
        "showTargetLink": "true"
        }
        }} />
    </div>

    <div class="yui-u">

    </div>
</div>

    <@agreementCommon.renderAgreementCommon type = 'sup-agreement'/>


<script type="text/javascript">//<![CDATA[

    <#if form.mode == 'create'>

    window.onload = function() {

        //Юр.лицо по умолчанию
        var searchUrl = Alfresco.constants.PROXY_URI + "/citeck/ecosConfig/ecos-config-value?configName=defaultLegalEntity";
        var request = new XMLHttpRequest();

        request.open('GET', searchUrl, false);  // `false` makes the request synchronous
        request.send(null);
            if (request.status === 200) {
                if (request.responseText) {
                    var data = eval('(' + request.responseText + ')');
                    var default_legal_entity = data;
                    if (data.value) {
                        var fieldLegalEntity = "${args.htmlid}_assoc_contracts_agreementLegalEntity-cntrl";
                        var legalEntityDesrc = Alfresco.util.ComponentManager.get(fieldLegalEntity);
                        legalEntityDesrc.selectItems(data.value);
                    }
                }
            }

        //Подписант по умолчанию
        var searchUrl = Alfresco.constants.PROXY_URI + "/citeck/ecosConfig/ecos-config-value?configName=defaultSignatory";
        var request = new XMLHttpRequest();

        request.open('GET', searchUrl, false);  // `false` makes the request synchronous
        request.send(null);
        if (request.status === 200) {
            if (request.responseText) {
                var data = eval('(' + request.responseText + ')');
                if (data.value != "") {
                    var fieldSignatory = "${args.htmlid}_assoc_idocs_signatory-cntrl";
                    var legalSignatoryDesrc = Alfresco.util.ComponentManager.get(fieldSignatory);
                    legalSignatoryDesrc.selectItems(data.value);
                }
            }
        }
    }

    </#if>
//]]></script>
<script type="text/javascript">//<![CDATA[
    <#if form.mode == 'create'>
    YAHOO.Bubbling.on("mandatoryControlValueUpdated", function (layer, args) {
        var control = args[1];
        if (control.id != "${args.htmlid}_prop_contracts_agreementDate-cntrl") return;

//var currentDate = control.getSelectedItems();

//if(currentDate.length == 0) return;

        var dateControl = Alfresco.util.ComponentManager.get("${args.htmlid}_prop_contracts_agreementDate-cntrl");
        var durationControl = Alfresco.util.ComponentManager.get("${args.htmlid}_prop_contracts_duration-cntrl");

        if (dateControl && durationControl) {
            var agreementDate = YAHOO.util.Dom.get(dateControl.currentValueHtmlId);


            if (agreementDate && agreementDate.value) {
                var agreementDateTime = Alfresco.util.fromISO8601(agreementDate.value);
                if (agreementDateTime) {
                    var newDate = new Date(Date.UTC(
                            agreementDateTime.getFullYear() + 1,
                            agreementDateTime.getMonth(),
                            agreementDateTime.getDate(),
                            0, 0, 0
                    ));
                    durationControl.setOptions({currentValue: Alfresco.util.toISO8601(newDate)});
                    durationControl.onReady();
                }
            }
        }

    });
    </#if>
//]]></script>
</@>