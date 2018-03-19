<#assign loadIndicator = (view.params.loadIndicator!"true") == "true" />
<#assign formMode = view.mode?string + "-form" />
<#assign formTemplate = "form-template-" + view.template?string />

<div id="${id}-form" class="ecos-form ${formMode} invariants-form ${formTemplate} <#if loadIndicator>loading</#if> <#if inlineEdit!false>inline-edit-form</#if>"
     data-bind="css: { <#if loadIndicator>'loading': !loaded(),</#if> 'submit-process': inSubmitProcess, invalid: invalid }">

<#if loadIndicator>
    <div class="loading-overlay">
        <div class="loading-container">
            <div class="loading-indicator"></div>
            <div class="loading-message">${msg('message.loading.form')}</div>
            <div class="submit-process-message">${msg('message.submit-process.form')}</div>
        </div>
    </div>
</#if>

<#if inlineEdit!false>
    <!-- ko if: node.loaded() && node().impl.loaded() -->
    <!-- ko with: resolve("node.impl") -->
    <!-- ko if: invalid -->
    <div class="form-errors">
        <div class="invalid-attributes">
            <span>${msg('message.invalid-attributes.form-errors')}:</span>
            <ul class="invalid-attributes-list" data-bind="foreach: getFilteredAttributes('invalid')">
                <li class="invalid-attribute" data-bind="click: $root.scrollToFormField, clickBubble: false">
                    <span class="invalid-attribute-name" data-bind="text: title"></span>:
                    <span class="invalid-attribute-message" data-bind="text: validationMessage"></span>
                </li>
            </ul>
        </div>
    </div>
    <!-- /ko -->
    <!-- /ko -->
    <!-- /ko -->
</#if>

<!-- ko API: rootObjects -->
<div class="form-fields" data-bind="with: node().impl">
    <!-- ko if: attributes().length != 0 -->
        <@views.renderElement view />
    <!-- /ko -->
</div>
<!-- /ko -->

<#if view.mode != 'view'>
    <div class="form-buttons" data-bind="with: node().impl">

     <#assign submitButtonTitle = view.params.submitButtonTitle!"button.send" />
     <#assign saveButtonTitle = view.params.saveButtonTitle!"button.save" />
     <#assign resetButtonTitle = view.params.resetButtonTitle!"button.reset" />
     <#assign cancelButtonTitle = view.params.cancelButtonTitle!"button.cancel" />

     <#if canBeDraft!false>
         <input id="${args.htmlid}-form-submit-and-send" type="submit" value="${msg(submitButtonTitle)}"
                data-bind="enable: valid() && !inSubmitProcess(), click: $root.submit.bind($root)" />

         <input id="${args.htmlid}-form-submit" type="submit" value="${msg(saveButtonTitle)}"
                data-bind="enable: validDraft() && !inSubmitProcess(), click: $root.submitDraft.bind($root)" />
     <#else>
         <input id="${id}-form-submit" type="submit" disabled="disabled"
                value="<#if view.mode == "create">${msg(submitButtonTitle)}<#else/>${msg(saveButtonTitle)}</#if>"
                data-bind="enable: $root.isSubmitReady, click: $root.submit.bind($root)" />
     </#if>

        <input id="${id}-form-reset"  type="button" value="${msg(resetButtonTitle)}" data-bind="enable: changed, click: reset" />
        <input id="${id}-form-cancel" type="button" value="${msg(cancelButtonTitle)}" data-bind="enable: true, click: $root.cancel.bind($root)" />
    </div>
</#if>

</div>

<script type="text/javascript">//<![CDATA[

<#assign runtimeKey = args.runtimeKey!args.htmlid />
<#assign virtualParent = (args.param_virtualParent!"false") == "true" />
<#assign baseRef = args.param_baseRef!""/>
<#assign rootAttributeName = args.param_rootAttributeName!""/>
<#assign invariantsRuntimeCache = view.params.invariantsRuntimeCache!"true" />
<#assign independent = (view.params.independent!"false") == "true" />

<#assign nodeKey>
    <#if independent>"${runtimeKey}"<#else><#if nodeRef?has_content>"${nodeRef}"<#else>"${runtimeKey}"</#if></#if>
</#assign>

<#escape x as x?js_string>
require(['citeck/components/invariants/invariants',
         'citeck/utils/knockout.invariants-controls',
         'citeck/utils/knockout.yui'], function(InvariantsRuntime) {

    new InvariantsRuntime("${args.htmlid}-form", "${runtimeKey}").setOptions({
        invariantsRuntimeCache: ${invariantsRuntimeCache},

        model: {
            key: "${runtimeKey}",
            parent: <#if args.param_parentRuntime?has_content>"${args.param_parentRuntime}"<#else>null</#if>,
            virtualParent: <#if virtualParent>"${args.param_parentRuntime}"<#else>null</#if>,
            baseRef: <#if baseRef??>"${baseRef}"<#else>null</#if>,
            rootAttributeName: <#if rootAttributeName??>"${rootAttributeName}"<#else>null</#if>,
            formTemplate: "${view.template}",
            independent: "${independent?string}",
            <#if inlineEdit!false>inlineEdit: true,</#if>

            node: {
                key: "${nodeKey?trim}",
                nodeRef: <#if nodeRef?has_content>"${nodeRef}"<#else>null</#if>,
                <#if type?has_content>type: "${type}",</#if>

                <#if classNames?has_content>classNames: <@views.renderQNames classNames />,</#if>
                <#if attributeNames?has_content>viewAttributeNames: <@views.renderValue attributeNames />,</#if>

                _set: <@views.renderValue attributeSet />,
                _invariants: <@views.renderInvariants invariants />,

                runtime: "${runtimeKey}",
                defaultModel: <@views.renderDefaultModel defaultModel />,
            }
        }
    });
});
</#escape>

//]]></script>