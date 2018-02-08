<#macro renderTemplate name="default" mode="" params={}>
	<#assign element = { "template": name, "type": "region", "params": params } />
	<@renderElement element />
</#macro>

<#macro renderElement element>
	<#-- Этот код решает баг - при <view kind="3-column"> (mode="view") двоится вывод значений - выводятся с template-text и template-default
		 поскольку этот модуль одна из основных FTL-шаблонов, пока закомментировал этот код
	-->
	<#--<#if !element.template?has_content && viewScope.view.mode == "view">
		<#return/>
	</#if>-->

	<#assign template = element.template!"default" />
	<#assign oldScope = viewScope!{} />
	<#global viewScope = oldScope + { element.type : element } />

	<#if (viewScope.view.template == "wide" || viewScope.view.template == "blockset") && element.type == "field">
		<#assign wideBlockWidth>
			<#-- field level -->
			<#if element.params.width?has_content>
				${element.params.width}

			<#-- view level -->
			<#elseif viewScope.view.params.width?has_content>
				${viewScope.view.params.width}

			<#-- root view level -->
			<#elseif view.params.width?has_content>
				${view.params.width}
			</#if>
		</#assign>
	</#if>

	<#-- virtual elements for field -->
	<#if element.attribute??>
		<!-- ko with: getAttribute("${element.attribute}") -->
		<#global fieldId = args.htmlid + "-" + element.attribute?replace(':', '_') />
		<#global globalAttributeName = element.attribute?string/>
	</#if>

	<#-- virtual elements for view -->
	<#if element.type == "view" && element.template?contains("set") && element.params.setId??>
		<!-- ko ifnot: ko.computed(function() {
			var set = getAttributeSet('${element.params.setId}');
			return set ? set.hidden() : false
		}) -->
	</#if>

	<#-- custom class name for div -->
	<#if viewScope.view.params.customClass??>
		<#assign customClass = viewScope.view.params.customClass!"" />
	<#else>
		<#assign customClass = "" />
	</#if>

	<div class="form-${element.type} template-${template} ${customClass}"
		<#if element.attribute??>
			data-bind="css: {
				invalid: invalid, 
				hidden: irrelevant, 
				'with-help': description, 
				'inline-edit': inlineEditVisibility
			}"
			data-attribute-name="${element.attribute}"
		</#if>

		<#-- custom width for field -->
		<#if element.type == "field" && wideBlockWidth?has_content>
			style="width: ${wideBlockWidth?trim};"
		</#if>
	>
		<@renderContent element />
	</div>

	<#if element.type == "view" && element.template?contains("set") && element.params.setId??>
		<!-- /ko -->
	</#if>

	<#if element.attribute??>
		<!-- /ko -->
	</#if>

	<#global viewScope = oldScope />
</#macro>

<#macro renderContent element>
	<#assign template = element.template!"default" />

	<#assign withoutMode = element.type == "region" && inlineEdit!false />

	<#if !withoutMode>
		<#assign file>/ru/citeck/views/${element.type}/${viewScope.view.mode}/${template}.ftl</#assign>
		<#if citeckUtils.templateExists(file)><#include file /><#return /></#if>
	</#if>

	<#assign file>/ru/citeck/views/${element.type}/${template}.ftl</#assign>
	<#if citeckUtils.templateExists(file)><#include file /><#return /></#if>

	<#if !withoutMode>
		<#assign file>/ru/citeck/views/${element.type}/${viewScope.view.mode}/default.ftl</#assign>
		<#if citeckUtils.templateExists(file)><#include file /><#return /></#if>
	</#if>

	<#assign file>/ru/citeck/views/${element.type}/default.ftl</#assign>
	<#if citeckUtils.templateExists(file)><#include file /><#return /></#if>
</#macro>

<#macro renderRegion name>
	<#list viewScope.field.regions as region>
		<#if region.name == name>
			<@renderElement region />
		</#if>
	</#list>
</#macro>

<#macro renderViewContainer view id>
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
</#macro>

<#function regionIsPresent name>
	<#list viewScope.field.regions as region>
		<#if region.name == name && region.template??>
			<#return true />
		</#if>
	</#list>
	<#return false />
</#function>

<#macro renderQNames qnames=[]>
	<#if qnames?has_content>
	[
		<#list qnames as qname>
			"${qname}"<#if qname_has_next>,</#if>
		</#list>
	]
	<#else>null</#if>
</#macro>

<#macro renderInvariant invariant><#escape x as jsonUtils.encodeJSONString(x)>{
	"scope": {
		<#assign invariantScope = invariant.scope!{} />
		"class": <#if invariantScope.class??>"${invariantScope.class}"<#else>null</#if>,
		"classKind": <#if invariantScope.classKind??>"${invariantScope.classKind}"<#else>null</#if>,
		"attribute": <#if invariantScope.attribute??>"${invariantScope.attribute}"<#else>null</#if>,
		"attributeKind": <#if invariantScope.attributeKind??>"${invariantScope.attributeKind}"<#else>null</#if>
	},
	"feature": "${invariant.feature?string}",
	"description": "${invariant.description!}",
	"final": ${invariant.final?string},
	"language": "${invariant.language}",
	"expression":
		<#assign value = invariant.expression />
		<#if value?is_sequence>
			[
			<#list value as item>
				<#if item?is_hash>
				{
					"attribute": "${item.attribute}",
					"predicate": "${item.predicate}",
					"value": <#if item.value??>"${item.value}"<#else>null</#if>
				}
				<#elseif item?is_string>
				"${item}"
				</#if><#if item_has_next>,</#if>
			</#list>
			]
		<#elseif value?is_string>
			"${value}"
		<#else>
			null
		</#if>
}</#escape></#macro>

<#macro renderInvariants invariants=[]>
	<#if invariants?has_content>
		[
			<#list invariants as invariant>
				<@renderInvariant invariant /><#if invariant_has_next>,</#if>
			</#list>
		]
	<#else>null</#if>
</#macro>

<#macro renderDefaultModel model>
	<#escape x as jsonUtils.encodeJSONString(x)>
	{
		"companyhome": <@views.renderValue model["companyhome"]!"" />,
		"userhome": <@views.renderValue model["userhome"]!"" />,
		"person": <@views.renderValue model["person"]!"" />,
		"view": <@views.renderValue model["view"]!"" />
	}
	</#escape>
</#macro>

<#macro renderValue value="">
	<#if !value??>
		null
	<#elseif value?is_hash>
		{
		<#list value?keys as key>
			"${key}": <@views.renderValue value[key] /><#if key_has_next>,</#if>
		</#list>
		}
	<#elseif value?is_string>
		"${value?js_string}"
	<#elseif value?is_number>
		${value?c}
	<#elseif value?is_boolean>
		${value?string}
	<#elseif value?is_enumerable>
		[ <#list value as item><@views.renderValue item /><#if item_has_next>,</#if></#list> ]
	</#if>
</#macro>

<#macro nodeViewStyles>
	<@link rel="stylesheet" href="${url.context}/res/citeck/components/invariants/invariants.css" group="node-view" />
	<@link rel="stylesheet" href="${url.context}/res/yui/calendar/assets/calendar.css" group="node-view"/>
	<@link rel="stylesheet" href="${url.context}/res/citeck/components/form/controls/user-profile.css" group="node-view"/>
</#macro>

<#macro nodeViewScripts>
	<@script src="${url.context}/res/yui/resize/resize.js" group="node-view"></@script>
	<@script src="${url.context}/res/yui/calendar/calendar.js" group="node-view"></@script>

	<@script src="${url.context}/res/citeck/components/form/constraints.js" group="node-view"></@script>

	<@script src="${url.context}/res/citeck/components/dynamic-tree/error-manager.js" group="node-view"></@script>
	<@script src="${url.context}/res/citeck/components/dynamic-tree/hierarchy-model.js" group="node-view"></@script>
	<@script src="${url.context}/res/citeck/components/dynamic-tree/criteria-model.js" group="node-view"></@script>
	<@script src="${url.context}/res/citeck/components/dynamic-tree/cell-formatters.js" group="node-view"></@script>
	<@script src="${url.context}/res/citeck/components/dynamic-tree/has-buttons.js" group="node-view"></@script>
</#macro>

<#macro nodeViewWidget nodeRef="" type="">
	<@inlineScript group="node-view">
		<#assign runtimeKey = args.runtimeKey!args.htmlid />
		<#assign virtualParent = (args.param_virtualParent!"false") == "true" />
		<#assign baseRef = args.param_baseRef!""/>
		<#assign rootAttributeName = args.param_rootAttributeName!""/>
		<#assign invariantsRuntimeCache = view.params.invariantsRuntimeCache!"true" />
		<#assign independent = (view.params.independent!"false") == "true" />

		<#assign nodeKey>
			<#if independent>"${runtimeKey}"<#else>
				<#if nodeRef?has_content>"${nodeRef}"<#else>"${runtimeKey}"</#if>
			</#if>
		</#assign>

		<#escape x as x?js_string>
		require(['citeck/components/invariants/invariants', 'citeck/utils/knockout.invariants-controls', 'citeck/utils/knockout.yui'], function(InvariantsRuntime) {
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
	</@>
</#macro>

