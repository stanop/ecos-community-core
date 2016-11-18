<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>

	<#if form.mode == "view">
		<#assign twoColumnClass = "yui-g plain" />
		<#assign threeColumnClass = "yui-gb plain" />
	<#else>
		<#assign twoColumnClass = "yui-g" />
		<#assign threeColumnClass = "yui-gb" />
	</#if>

	<@forms.renderField field="prop_cardlet_cardMode" extension = {
		<#--"help": "Leave empty for default mode, input 'all' (without quotes) for all modes"-->
		"help": "Оставьте пустым для режима по умолчанию, введите 'all' (без кавычек) для всех режимов"
	} />

	<@forms.renderField field="prop_cardlet_regionId" extension = {
		"control": {
			"template": "/ru/citeck/components/form/controls/select.ftl",
			"params": {
				"optionsUrl": "${url.context}/page/citeck/component?scope=page&source-id=card-details&short=true",
				"resultsList": "components",
				"valueField": "region-id",
				"titleField": "region-id",
				"sortKey": "region-id"
			}
		}
	} />

	<div class="${twoColumnClass}">
		<div class="yui-u first">
	<@forms.renderField field="prop_cardlet_regionColumn" />
		</div>
		<div class="yui-u">
	<@forms.renderField field="prop_cardlet_regionPosition" />
		</div>
	</div>

	<#-- parameters for mobile version -->
	<div class="${twoColumnClass}">
		<div class="yui-u first">
			<@forms.renderField field="prop_cardlet_availableInMobile" />
		</div>
		<div class="yui-u">
			<@forms.renderField field="prop_cardlet_positionIndexInMobile" />
		</div>
	</div>
	<#-- end of parameters for mobile version -->


	<@forms.renderField field="prop_cardlet_allowedType" />

	<@forms.renderField field="prop_cardlet_allowedAuthorities" />

	<@forms.renderField field="prop_cardlet_condition" extension = extensions.controls.textarea />


</@>
