<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<#assign russianPassportCondition = "prop_tk_kind == 'workspace://SpacesStore/idocs-cat-dockind-passport-rus'" />
<#assign otherPassportCondition = "prop_tk_kind == 'workspace://SpacesStore/idocs-cat-dockind-passport-other'" />

<@forms.fileUploadSupport />

<#if form.mode == "create">
<@forms.setMandatoryFields
fieldNames = [
	"prop_cm_content"
] />
</#if>

<@forms.setMandatoryFields
fieldNames = [
	"prop_pass_series",
	"prop_pass_number",
	"prop_pass_issueDate",
	"prop_pass_issuingAuthority",
	"prop_pass_subdivisionCode"
] condition = russianPassportCondition />

<@forms.setMandatoryFields
fieldNames = [
	"prop_pass_info"
] condition = otherPassportCondition />

<@forms.renderFormsRuntime formId=formId />

<#if form.mode == "view">
	<#assign twoColumnClass = "yui-g " />
	<#assign threeColumnClass = "yui-gb " />
<#else>
	<#assign twoColumnClass = "yui-g" />
	<#assign threeColumnClass = "yui-gb" />
</#if>

<@formLib.renderFormContainer formId=formId>

	<#if form.mode = "create">
		<#assign typeDefault = { "value": "workspace://SpacesStore/idocs-cat-dockind-passport-rus" } />
	<#else/>
		<#assign typeDefault = {} />
	</#if>
	
	<div class="${twoColumnClass}">
		<div class="yui-u first">
			<@forms.renderField field="prop_tk_kind" extension = typeDefault + {
				"label": "Вид документа",
				"control" : {
					"template" : "/ru/citeck/components/form/controls/select.ftl",
					"params": {
						"optionsUrl": "${url.context}/proxy/alfresco/citeck/subcategories?nodeRef=workspace://SpacesStore/idocs-cat-doctype-passport",
						"titleField": "name",
						"valueField": "nodeRef",
						"resultsList": "nodes"
					}
				}
			} />
		</div>
		<div class="yui-u">
			<#assign personControl = {
				"control": {
					"template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
					"params": {}
				}
			} />
			<#if form.mode == "create">
				<#if args.assoc_pass_person??>
					<@forms.renderField field="assoc_pass_person" extension = personControl + {
						"value": "${args.assoc_pass_person}",
						"disabled": true
					} />
				<#else/>
					<@forms.renderField field="assoc_pass_person" extension = personControl />
				</#if>
			<#elseif form.mode == "edit" />
				<@forms.renderField field="assoc_pass_person" extension = personControl + {
					"disabled": true
				} />
			<#elseif form.mode == "view" />
				<@forms.renderField field="assoc_pass_person" extension = personControl />
			</#if>
		</div>
	</div>
	
	<@forms.displayConditional 'prop_tk_kind' 'workspace://SpacesStore/idocs-cat-dockind-passport-rus'>
		<div class="${twoColumnClass}">
			<div class="yui-u first">
				<@forms.renderField field="prop_pass_series" />
			</div>
			<div class="yui-u">
				<@forms.renderField field="prop_pass_number" />
			</div>
		</div>
		
		<@forms.renderField field="prop_pass_issuingAuthority" extension = extensions.controls.textarea />
		
		<div class="${twoColumnClass}">
			<div class="yui-u first">
				<@forms.renderField field="prop_pass_issueDate" />
			</div>
			<div class="yui-u">
				<@forms.renderField field="prop_pass_subdivisionCode" />
			</div>
		</div>
	</@>
	
	<@forms.displayConditional 'prop_tk_kind' 'workspace://SpacesStore/idocs-cat-dockind-passport-other'>
		<@forms.renderField field="prop_pass_info" extension = extensions.controls.textarea />
	</@>
	
	<#if form.mode == "view" && (form.arguments.formId!"") == "preview">
		<@forms.renderField field="prop_cm_content" extension = extensions.controls.fileupload + {
			"label": "Скан-копия документа",
			"control": {
				"template": "/ru/citeck/components/form/controls/img-preview.ftl",
				"params": {
					"width": "800px"
				}
			}
		} />
	<#elseif form.mode != "edit">
		<@forms.renderField field="prop_cm_content" extension = extensions.controls.fileupload + {
			"label": "Скан-копия документа"
		} />
	</#if>

	<#if form.mode == 'view'>
		<@forms.renderField field="prop_privacy_consent" extension = {
			"control": {
				"template": "/ru/citeck/components/form/controls/boolean-view.ftl",
				"params": {
					"trueLabel": "Согласие на обработку персональных данных получено",
					"falseLabel": "Согласие на обработку персональных данных НЕ получено"
				}
			}
		} />
	<#else/>
		<#assign consentText>Настоящим подтверждаю мое согласие на  обработку и хранение моих  персональных  данных. 
			В случае размещения мной  персональных данных иных  лиц, гарантирую, 
			что мной  получено их  согласие на предоставление, обработку и хранение их персональных данных.</#assign>
		<@forms.renderField field="prop_privacy_consent" extension = {
			"label": consentText
		} />
	</#if>

</@>
