<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />
<#import "/ru/citeck/components/form/contracts/agreement-common.ftl" as agreementCommon />

<@forms.setMandatoryFields
fieldNames = [
"assoc_contracts_agreementLegalEntity",
"assoc_contracts_contractor",
"prop_contracts_agreementNumber",
"prop_tk_kind"
]/>

<@forms.fileUploadSupport />

<@forms.setMandatoryFields fieldNames = [ "prop_cm_content" ] condition="prop_dms_updateContent == 'false'" />

<#if form.mode == "create">
	<@forms.formConfirmSupport createBy=createBy message="Все несохраненные данные будут потеряны" />
</#if>

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

	<@agreementCommon.renderAgreementCommon type = 'agreement'/>

<script type="text/javascript">//<![CDATA[

    <#if form.mode == "create">

        window.onload = function() {

// Юр.лицо по умолчанию
        var searchUrl = Alfresco.constants.PROXY_URI + "/citeck/ecosConfig/ecos-config-value?configName=defaultLegalEntity";
        var request = new XMLHttpRequest();

        request.open('GET', searchUrl, false);  // `false` makes the request synchronous
        request.send(null);
        if (request.status === 200) {
            if (request.responseText) {
                var data = eval('(' + request.responseText + ')');
                    if (data.value != "") {
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

	<#if form.mode == "create">
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