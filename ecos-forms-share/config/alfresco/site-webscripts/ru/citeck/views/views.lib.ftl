<#macro renderTemplate name="default" mode="" params={}>
	<#assign element = { "template": name, "type": "region", "params": params } />
	<@renderElement element />
</#macro>

<#macro renderElement element>
	<#assign template = element.template!"default" />
	<#assign inlineEdit = (view.params.inlineEdit!"false") == "true" />
	<#assign oldScope = viewScope!{} />
	<#global viewScope = oldScope + { element.type : element } />

	<#if element.attribute??>
		<!-- ko with: attribute("${element.attribute}") -->
		<#global fieldId = args.htmlid + "-" + element.attribute?replace(':', '_') />
	</#if>

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
	

	<div class="form-${element.type} template-${template}"
		<#if element.attribute??>data-bind="css: { invalid: invalid, hidden: irrelevant, 'with-help': description }"</#if>

		<#-- custom width for field -->
		<#if element.type == "field" && wideBlockWidth?has_content>
			style="width: ${wideBlockWidth?trim};"
		</#if>
	>
		<@renderContent element />
	</div>
	
	<#if element.attribute??>
		<!-- /ko -->
	</#if>
	
	<#global viewScope = oldScope />
</#macro>

<#macro renderContent element>
	<#assign template = element.template!"default" />

	<#assign inlineEdit = (view.params.inlineEdit!"false") == "true" />
	<#assign withoutMode = element.type == "region" && inlineEdit />

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
	<#assign inlineEdit = (view.params.inlineEdit!"false") == "true" />

	<#assign formMode = view.mode?string + "-form" />
	<#assign formTemplate = "form-template-" + view.template?string />

	<#assign viewMode = view.mode?string == "view" />

	<div id="${id}-form" class="ecos-form ${formMode} invariants-form ${formTemplate} <#if loadIndicator>loading</#if> <#if inlineEdit && viewMode>inline-edit-form</#if>"
		 data-bind="css: { <#if loadIndicator>'loading': !loaded(),</#if> 'submit-process': inSubmitProcess }">

		<#if loadIndicator>
			<div class="loading-overlay">
				<div class="loading-container">
					<div class="loading-indicator"></div>
					<div class="loading-message">${msg('message.loading.form')}</div>
					<div class="submit-process-message">${msg('message.submit-process.form')}</div>
				</div>
			</div>
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
                <input id="${id}-form-submit" type="submit"
                       value="<#if view.mode == "create">${msg(submitButtonTitle)}<#else/>${msg(saveButtonTitle)}</#if>"
                       data-bind="enable: valid() && !inSubmitProcess(), click: $root.submit.bind($root)" />
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

<#macro renderQNames qnames>
	[
		<#list qnames as qname>
			"${qname}"<#if qname_has_next>,</#if>
		</#list>
	]
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

<#macro renderInvariants invariants>
	[
		<#list invariants as invariant>
			<@renderInvariant invariant /><#if invariant_has_next>,</#if>
		</#list>
	]
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
		<#assign loadAttributesMethod = view.params.loadAttributesMethod!"default" />
		<#assign loadGroupIndicator = view.params.loadGroupIndicator!"false" />
		<#assign preloadInvariants = view.params.preloadInvariants!"false" />

		<#escape x as x?js_string>
		require(['citeck/components/invariants/invariants', 'citeck/utils/knockout.invariants-controls', 'citeck/utils/knockout.yui'], function(InvariantsRuntime) {
			new InvariantsRuntime("${args.htmlid}-form", "${runtimeKey}").setOptions({
				model: {
					key: "${runtimeKey}",
					parent: <#if args.param_parentRuntime?has_content>"${args.param_parentRuntime}"<#else>null</#if>,
					formTemplate: "${view.template}",

					loadAttributesMethod: "${loadAttributesMethod}",
					loadGroupIndicator: ${loadGroupIndicator},
					preloadInvariants: ${preloadInvariants},

					node: {
						key: "${runtimeKey}",
						virtualParent: <#if (args.param_virtualParent!"false") == "true">"${args.param_parentRuntime}"<#else>null</#if>,
						nodeRef: <#if nodeRef?has_content>"${nodeRef}"<#else>null</#if>,
						type: <#if type?has_content>"${type}"<#else>null</#if>,
						
						classNames: <#if classNames??><@views.renderQNames classNames /><#else>null</#if>,
						groups: [
							<#if groups?? && groups?has_content>
								<#list groups as group>
								{
									"id": <@views.renderValue group.id />,
									"index": <@views.renderValue group.index />,
									"attributes": <@views.renderValue group.attributes />,
									"invariants": <@views.renderInvariants group.invariants />
								}<#if group_has_next>,</#if>
								</#list>
							</#if>
						],

						<#if loadAttributesMethod != "clickOnGroup">
							forcedAttributes: <@views.renderValue attributes />,
						</#if>

						runtime: "${runtimeKey}",
						defaultModel: <@views.renderDefaultModel defaultModel />,
					},

					invariantSet: {
						key: "${runtimeKey}",

						<#if loadAttributesMethod != "clickOnGroup">
							forcedInvariants: <@views.renderInvariants invariants />
						</#if>
					}
				}
			});
		});
		</#escape>
	</@>
</#macro>

