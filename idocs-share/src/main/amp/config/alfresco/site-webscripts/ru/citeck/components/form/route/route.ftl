<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<#if formUI == "true">
    <@formLib.renderFormsRuntime formId=formId />
</#if>

<@formLib.renderFormContainer formId=formId>
    <#if form.mode != "view">
        <div class="set-title bold upcase">${msg("routes.set.name")}</div>
    </#if>

    <@forms.renderField field="prop_cm_name" />

    <@forms.renderField field="prop_tk_appliesToType" extension = {
        "control" : {
            "template" : "/ru/citeck/components/form/controls/types-and-kinds.ftl",
            "params": {
                "typeFieldId": "prop_tk_appliesToType",
                "kindFieldId": "prop_tk_appliesToKind",

                "blank": "both",
                "recurse": "true"
            }
        }
    } />

    <#if form.mode != "view">
        <div class="set-title bold upcase">${msg("routes.set.stages")}</div>
    </#if>


    <@forms.renderField field = "assoc_route_stages" extension = { 
        "control" : {
            "template" : "/ru/citeck/components/form/controls/route.ftl",
            "params" : {
                "allowedAuthorityType": "USER, GROUP",
                "allowedGroupType": "ROLE, BRANCH"
            }   
        }
     } />

    <@forms.renderField field="prop_cm_taggable" />

</@>
