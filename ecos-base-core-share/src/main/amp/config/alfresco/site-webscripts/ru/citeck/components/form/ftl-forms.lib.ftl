<#assign infoField = { "control": { "template": "/org/alfresco/components/form/controls/info.ftl" } } />
<#assign textField = { "control": { "template": "/org/alfresco/components/form/controls/textarea.ftl", "params": {} } } />

<#assign default_extensions = {
	"prop_bpm_dueDate": infoField,
	"bpm_comment": textField,
	"prop_message": infoField
} />

<#macro mode mode1 mode2="">
<#if form.mode == mode1 || form.mode == mode2>
<#nested />
</#if>
</#macro>

<#global validators = {
	"mandatory": {
		"validationHandler": "Alfresco.forms.validation.mandatory",
		"params": "{}",
		"event": "keyup"
	},
	"mandatoryIf": {
		"validationHandler": "Alfresco.forms.validation.mandatoryIf",
		"params": "{ condition: 'false' }",
		"event": "keyup"
	}
} />

<#global extensions = {
	"search": {
		"text": {
			"control": {
				"template": "/ru/citeck/components/form/controls/text-richsearch.ftl",
				"params": {}
			}
		},
		"number": {
			"control": {
				"template": "/ru/citeck/components/form/controls/number-richsearch.ftl",
				"params": {}
			}
		},
		"boolean": {
			"control": {
				"template": "/ru/citeck/components/form/controls/boolean-richsearch.ftl",
				"params": {}
			}
		},
		"select": {
			"control": {
				"template": "/ru/citeck/components/form/controls/select-richsearch.ftl",
				"params": {}
			}
		},
		"date": {
			"control": {
				"template": "/org/alfresco/components/form/controls/daterange.ftl",
				"params": {}
			}
		}
	},
	"controls": {
		"textarea": {
			"control": {
				"template": "/org/alfresco/components/form/controls/textarea.ftl",
				"params": {
					"style": "width:95%"
				}
			}
		},
		"number": {
			"control": {
				"template": "/org/alfresco/components/form/controls/textfield.ftl",
				"params": {
					"style": "width:100px"
				}
			}
		},
		"info": {
			"control": {
				"template": "/org/alfresco/components/form/controls/info.ftl",
				"params": {
				}
			}
		},
		"userName": {
			"control": {
				"template": "/ru/citeck/components/form/controls/username.ftl",
				"params": {
				}
			}
		},
		"hidden": {
			"control": {
				"template": "/ru/citeck/components/form/controls/always-hidden-textfield.ftl",
				"params": {
				}
			}
		},
		"orgstruct": {
			"control": {
				"template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
				"params": {}
			}
		},
		"fileupload": {
			"control": {
				"template": "/ru/citeck/components/form/controls/fileUpload.ftl",
				"params": {
					"style": "width:50px"
				}
			}
		}
	},
	"workflow": {
		"priority": {
			"label": msg("workflow.field.priority"),
			"control": {
				"template": "/org/alfresco/components/form/controls/workflow/priority.ftl",
				"params": {
				}
			}
		}
	},
	"properties": {
		"readOnly": {
			"disabled": true
		}
	}
} />

<#macro suppressMandatoryConstraints>
    <#assign constraints = [] />
    <#list form.constraints as constraint>
        <#if !constraint.validationHandler?contains("mandatory")>
            <#assign constraints = constraints + [constraint] />
        </#if>
    </#list>
    <#global form = form + { "constraints": constraints } />
</#macro>

<#macro displayConditional field value="*">
	<#if form.mode == 'view'>
		<#if form.data[field]?? && form.data[field] == value>
			<#nested />
		</#if>
	<#else/>
		<#assign blockId = "${args.htmlid}-condition-${field}-equals-${value}" />
		<script type="text/javascript">//<[!CDATA[
			Citeck.forms.displayConditional("${blockId}", <#if value == "*">"${field} != null"<#else>"${field} == '${value}'"</#if>, [
				"${args.htmlid}_${field}",
				"${args.htmlid}_${field}-entry",
				"${args.htmlid}_${field}-cntrl"
			]);
		//]]></script>
		<div id="${blockId}">
			<#nested />
		</div>
	</#if>
</#macro>

<#macro computedField field value triggers>
	<#if form.mode != 'view'>
		<input id="${args.htmlid}_${field}" type="hidden" name="${field}" />
		<script type="text/javascript">//<[!CDATA[
			Citeck.forms.valueComputed("${args.htmlid}_${field}", "${value?js_string}", [
			<#list triggers as trigger>
				"${args.htmlid}_${trigger}",
				"${args.htmlid}_${trigger}-entry"<#if trigger_has_next>,</#if>
			</#list>
			]);
		//]]></script>
	</#if>
</#macro>

<#macro fileUploadSupport>
	<#if form.submissionUrl?matches("[?]", "r")>
		<#assign url = form.submissionUrl + "&format=html" />
	<#else/>
		<#assign url = form.submissionUrl + "?format=html" />
	</#if>
	<#global form = form + {
		"enctype": "multipart/form-data",
		"submissionUrl": url
	} />
</#macro>

<#macro setMandatoryFields fieldNames condition="">
	<#list fieldNames as name>
	<#if form.fields[name]??>
		<#assign fields = form.fields />
		<#assign field = fields[name] />
		<#assign field2 = field + { "mandatory": true, "endpointMandatory": true } />
		<#assign fields2 = fields + { name: field2 } />
		
		<#if condition == "">
			<#assign constraint = { "fieldId": name } + validators.mandatory />
		<#else>
			<#assign constraint = validators.mandatoryIf + { 
				"fieldId": name, 
				"params": "{ condition: '" + condition?replace("'", "\\'") + "' }" 
			} />
		</#if>
		<#assign constraints2 = form.constraints + [ constraint ] />

		<#global form = form + { "fields": fields2, "constraints": constraints2 } />
	</#if>
	</#list>
</#macro>

<#macro print_hash h>
	<#if debug!false>
		<#list h?keys as key>
			<#if h[key]??>
				<#if h[key]?is_string || h[key]?is_date || h[key]?is_number>
${key} = ${h[key]}
				<#elseif h[key]?is_boolean>
${key} = <#if h[key]>true<#else>false</#if>
				<#else>
${key} = somewhat
				</#if>
			</#if>
		</#list>
	</#if>
</#macro>

<#macro renderFieldImpl field extension>
	<#assign f = form.fields[field] />

	<#if default_extensions[field]??>
		<#assign f = f + default_extensions[field] />
	</#if>

	<#if extension?keys?size != 0>
		<#assign f = f + extension />
	</#if>

	<#if my_extensions?? && my_extensions[field]??>
		<#assign f = f + my_extensions[field] />
	</#if>

	<@print_hash h=f />
	<@formLib.renderField field = f />
</#macro>

<#macro renderIndependentField field extension={}>
	<#assign f = field />

	<#if extension?keys?size != 0>
		<#assign f = f + extension />
	</#if>

	<@print_hash h=f />
	<@formLib.renderField field = f />
</#macro>

<#macro renderField field extension={}>
	<#if form?? && form.fields?? && form.fields[field]??>
		<@renderFieldImpl field=field extension=extension />
	</#if>
</#macro>

<#macro renderFieldIfNotEmpty field extension={}>
	<#if form?? && form.fields?? && form.fields[field]?? && form.fields[field].value != "">
		<@renderFieldImpl field=field extension=extension />
	</#if>
</#macro>

<#macro renderFields fields>
	<#list fields as field>
		<@renderField field=field />
	</#list>
</#macro>

<#macro renderFieldsExt fields>
	<#list fields?keys as field>
		<@renderField field=field extension=fields[field] />
	</#list>
</#macro>

<#macro numberField field extension={}>
	<#assign num = { "control": { 
		"template": "/org/alfresco/components/form/controls/textfield.ftl", 
		"params": { "style" : "width: 100px;" } 
	} } />
	<@renderField field=field extension=num + extension />
</#macro>

<#macro categoryField field location extension={}>
	<#assign e = { "control": {
		"template": "/ru/citeck/components/form/controls/select.ftl",
		"params": { 
			"optionsUrl": "${url.context}/proxy/alfresco/slingshot/doclib/categorynode/node/alfresco/category/root/${location?url}?perms=false&children=true", 
			"responseType": "YAHOO.util.DataSource.TYPE_JSON",
			"responseSchema": "{ resultsList: 'items', fields: [ {key:'nodeRef'}, {key:'name'} ] }",
			"valueField": "nodeRef",
			"titleField": "name"
		}
	} } />
	<@renderField field=field extension=e+extension />
</#macro>

<#macro formPromptSupport formId messagesJsonStr>
	<script type="text/javascript">
	//<![CDATA[
		Citeck.forms.promptBeforeSubmit("${formId}", YAHOO.lang.JSON.parse("${messagesJsonStr}"));
	//]]>
	</script>
</#macro>

<#macro formConfirmSupport formId message>
	<script type="text/javascript">
	//<![CDATA[
		window.onbeforeunload = function(){
			return '${message}';
		};
		YAHOO.util.Event.on("${formId}", "submit", function () {
			window.onbeforeunload = null;
		});
	//]]>
	</script>
</#macro>

<#function findOutcomeField fields>
    <#if fields["prop_bpm_outcomePropertyName"]??>
        <#assign outcome = fields["prop_bpm_outcomePropertyName"].value />
        <#assign outcomeLocalName = outcome?substring(outcome?index_of("}") + 1, outcome?length)/>
        <#list fields?keys as field>
            <#if fields[field].id?matches('prop_(.)*_' + outcomeLocalName)>
                <#return fields[field]>
            </#if>
        </#list>
    <#else>
        <#-- For forms with xml definition -->
        <#list fields?keys as field>
            <#if (fields[field].id?lower_case)?ends_with('outcome')>
                <#return fields[field]>
            </#if>
        </#list>
    </#if>
</#function>

<#function parseOutcomes options>
	<#assign outcomeOptions = options?split("#alf#") />
	<#assign outcomes = [] />
	<#list outcomeOptions as option>
		<#assign outcomes = outcomes + [ option?split('|')[0] ] />
	</#list>
	<#return outcomes />
</#function>

<#function parseOutcomeLabels options>
	<#assign outcomeOptions = options?split("#alf#") />
	<#assign outcomeLabels = {} />
	<#list outcomeOptions as option>
		<#assign keyValue = option?split('|') />
		<#assign outcomeLabels = outcomeLabels + { 
			keyValue[0] : keyValue[1]
		} />
	</#list>
	<#return outcomeLabels />
</#function>

<#function constructOutcomeOptions outcomes outcomeLabels>
	<#assign outcomeOptions><#list outcomes as outcome>${outcome}|${outcomeLabels[outcome]!outcome}<#if outcome_has_next>#alf#</#if></#list></#assign>
	<#return outcomeOptions />
</#function>

<#macro disableSubmitMoreOneTime formId>
<script type="text/javascript">//<![CDATA[
	YAHOO.util.Event.onContentReady("${formId}-submit-button", function() {
		var disabled = false;
		YAHOO.util.Event.on("${formId}", "submit", function () {
			if(disabled) return;
            YAHOO.util.Dom.setAttribute("${formId}-submit-button", "disabled", "disabled");
			disabled = true;
		});
        YAHOO.Bubbling.on("formValidationError", function() {
            if(!disabled) return;
            YAHOO.util.Dom.get("${formId}-submit-button").removeAttribute("disabled");
            disabled = false;
        });
    });
//]]></script>
</#macro>

<#macro renderFormsRuntime formId>
    <#if (args.formUI!'true') == 'true'>
        <@formLib.renderFormsRuntime formId=formId />
        <script type="text/javascript">//<![CDATA[
            YAHOO.Bubbling.on("beforeFormRuntimeInit", function(layer, args) {
                var runtime = args[1].runtime;
                if(runtime.formId != "${formId}") return;
                runtime.setShowSubmitStateDynamically(true);
            });
        //]]></script>
    </#if>
</#macro>
