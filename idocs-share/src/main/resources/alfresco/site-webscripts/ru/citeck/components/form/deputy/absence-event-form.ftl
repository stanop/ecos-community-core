<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.disableSubmitMoreOneTime formId=formId/>

<@forms.setMandatoryFields
fieldNames = [

]/>

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

<@formLib.renderFormContainer formId=formId>

    <div class="${twoColumnClass}">
        <div class="yui-u first">
            <@forms.renderField field="prop_deputy_startAbsence" extension={
                "control": {
                    "template": "/org/alfresco/components/form/controls/date.ftl",
                    "params": {
                        "showTime": "true"
                    }
                }
            }/>
        </div>
        <div class="yui-u">
            <@forms.renderField field="prop_deputy_endAbsence" extension={
                "control": {
                    "template": "/org/alfresco/components/form/controls/date.ftl",
                    "params": {
                        "showTime": "true"
                    }
                }
            }/>
        </div>
    </div>

    <@forms.renderField field="assoc_deputy_reasonOfAbsence" extension={
        "control": {
            "template": "/ru/citeck/components/form/controls/select.ftl",
            "params": {
                "optionsUrl": "/share/proxy/alfresco/citeck/search/simple?type=deputy:absenceReason&properties=deputy:reason",
                "resultsList": "nodes",
                "valueField": "nodeRef",
                "titleField": "deputy:reason"
            }
        }
    }/>

    <@forms.renderField field="prop_deputy_comment" />

</@>
