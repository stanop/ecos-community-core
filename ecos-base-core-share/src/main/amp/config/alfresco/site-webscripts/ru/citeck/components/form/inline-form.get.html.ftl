<@standalone>

    <@markup id="widgets">
        <@createWidgets/>
    </@>

    <@markup id="html">
        <@uniqueIdDiv>
            <#import "/org/alfresco/components/form/form.lib.ftl" as formLib />
            <#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

            <#if error?exists>
            <div class="error">${error}</div>
            <#elseif form?exists>
                <#assign formId=args.htmlid?js_string + "-form">
                <#assign formUI><#if args.formUI??>${args.formUI}<#else>true</#if></#assign>

                <#if config.scoped[args.itemId].forms.getForm(args.formId)??>
                    <#if form.viewTemplate?? && form.mode == "view">
                        <#include "${form.viewTemplate}" />
                    <#elseif form.editTemplate?? && form.mode == "edit">
                        <#include "${form.editTemplate}" />
                    <#elseif form.createTemplate?? && form.mode == "create">
                        <#include "${form.createTemplate}" />
                    <#else>
                        <@forms.renderFormsRuntime formId=formId />
                    
                        <@formLib.renderFormContainer formId=formId>
                            <#list form.structure as item>
                                <#if item.kind == "set">
                                    <@formLib.renderSet set=item />
                                <#else>
                                    <@formLib.renderField field=form.fields[item.id] />
                                </#if>
                            </#list>
                        </@>
                    </#if>
                <#else>
                    <@forms.renderFormsRuntime formId=formId />
                
                    <@formLib.renderFormContainer formId=formId>
                        <@formLib.renderField field = form.fields["prop_bpm_comment"]  + {
                            "label": msg("workflow.field.comment"),
                            "control": {
                                "template": "/org/alfresco/components/form/controls/textarea.ftl",
                                "params": {
                                    "style": "width:95%"
                                }
                            }
                        } />
                        <#assign outcomeField = forms.findOutcomeField(form.fields)!{} />
                        <#if outcomeField.id??>
                        <@formLib.renderField field = outcomeField + {
                            "control": {
                                "template": "/org/alfresco/components/form/controls/workflow/activiti-transitions.ftl",
                                "params": outcomeField.control.params
                            }
                        } />
                        </#if>
                    </@>
                </#if>
            <#else>
                <div class="form-container">${msg("form.not.present")}</div>
            </#if>
        </@>
    </@>
</@>
