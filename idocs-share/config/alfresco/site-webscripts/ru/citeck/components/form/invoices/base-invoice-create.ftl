<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields
    fieldNames = [
        "prop_invoice_name",
        "prop_invoice_date",
        "prop_invoice_number"
]/>

<@forms.fileUploadSupport />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>

    <#-- Наименование -->
    <@forms.renderField field="prop_invoice_name" extension = {
        "mandatory": true,
        "label": "${msg(\"props.title.invoice.name\")}"
    }/>

    <#-- Номер -->
    <@forms.renderField field="prop_invoice_number" extension = {
        "mandatory": true,
        "label": "${msg(\"props.title.invoice.number\")}"
    }/>

    <#-- Дата -->
    <@forms.renderField field="prop_invoice_date" extension = {
        "mandatory": true,
        "label": "${msg(\"props.title.invoice.date\")}",
        "control": {
            "template": "/ru/citeck/components/form/controls/date.ftl",
            "params": {
                "appendDaysToCurrentValue": 0
            }
        }

    }/>

    <#-- Описание -->
    <@forms.renderField field="iprop_nvoice_description" extension = {
        "label": "${msg(\"props.title.invoice.description\")}",
        "control": {
            "template": "/org/alfresco/components/form/controls/textarea.ftl",
            "params": {}
        }
    }/>

    <#-- Сумма -->
    <@forms.renderField field="prop_invoice_sum" extension = {
        "label": "${msg(\"props.title.invoice.sum\")}"
    }/>

    <#-- Юридическое лицо -->
    <@forms.renderField field="assoc_invoice_legalEntity" extension = {
        "label": "${msg(\"props.title.invoice.legalEntity\")}",
        "control": {
            "template": "/ru/citeck/components/form/controls/association_search.ftl",
            "params": {
                "searchWholeRepo": "true"
            }
        }
    }/>

    <#-- Контрагент -->
    <@forms.renderField field="assoc_invoice_contractor" extension = {
        "label": "${msg(\"props.title.invoice.contractor\")}",
        "control": {
            "template": "/ru/citeck/components/form/controls/association_search.ftl",
            "params": {
                "searchWholeRepo": "true"
            }
        }
    }/>

    <#-- Контент -->
    <@forms.renderField field="prop_cm_content" extension = {
        "endpointType": "cm:content",
        "mandatory": true,
        "control": {
            "template": "/ru/citeck/components/form/controls/fileUpload.ftl"
        }
    } />

</@>