<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<#function parseOutcomeLabels customOptions defaultOptions>
    <#if defaultOptions?has_content>
        <#assign defaultLabels = forms.parseOutcomeLabels(defaultOptions) />
    <#else>
        <#assign defaultLabels = {} />
    </#if>
    <#assign outcomeOptions = customOptions?split("#alf#") />
    <#assign outcomeLabels = {} />
    <#list outcomeOptions as option>
        <#assign keyValue = option?split('|') />
        <#assign key = keyValue[0] />
        <#if keyValue?size == 2>
            <#assign value = keyValue[1] />
        <#elseif defaultLabels[key]??>
            <#assign value = defaultLabels[key] />
        <#else>
            <#assign value = key />
        </#if>
        <#assign outcomeLabels = outcomeLabels + { key : value } />
    </#list>
    <#return outcomeLabels />
</#function>

<#assign outcomeField = forms.findOutcomeField(form.fields)!{} />

<#if outcomeField.id??>

    <#if form.fields["prop_wfcp_performOutcomes"]?? && form.fields["prop_wfcp_performOutcomes"].value?has_content>
        <#assign customOutcomes = form.fields["prop_wfcp_performOutcomes"].value />
        <#if !(outcomes??)>
            <#assign outcomes = forms.parseOutcomes(customOutcomes) />
        </#if>
        <#if !(outcomeLabels??)>
            <#assign defaultOutcomes = (outcomeField.control.params.options)!"" />
            <#assign outcomeLabels = parseOutcomeLabels(customOutcomes, defaultOutcomes) />
        </#if>
    </#if>

    <#if !(outcomes??)>
        <#assign outcomes = forms.parseOutcomes(outcomeField.control.params.options) />
    </#if>
    <#if !(outcomeLabels??)>
        <#assign outcomeLabels = forms.parseOutcomeLabels(outcomeField.control.params.options) />
    </#if>

    <@formLib.renderField field = outcomeField + {
        "control": {
            "template": "/ru/citeck/components/form/controls/workflow/activiti-transitions.ftl",
            "params": outcomeField.control.params + {
                "options": forms.constructOutcomeOptions(outcomes, outcomeLabels)
            }
        }
    } />
</#if>