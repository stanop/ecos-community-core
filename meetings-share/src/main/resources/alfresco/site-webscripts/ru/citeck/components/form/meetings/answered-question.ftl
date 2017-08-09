<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields
fieldNames = [
	"prop_meet_question"
]/>

<#if formUI == "true">
	<@formLib.renderFormsRuntime formId=formId />
</#if>

<#if form.mode == "view">
	<#assign twoColumnClass = "yui-g plain" />
	<#assign threeColumnClass = "yui-gb plain" />
<#else>
	<#assign twoColumnClass = "yui-g" />
	<#assign threeColumnClass = "yui-gb" />
</#if>

<@formLib.renderFormContainer formId=formId>

<#if form.mode == "create" >
	<input id="alf_assoctype" value="meet:childAnsweredQuestions" class="hidden" />
</#if>

<#if form.mode != "view" >
	<#assign showAddButton = "true" />
<#else>
	<#assign showAddButton = "false" />
</#if>
<@forms.renderField field="assoc_meet_askedQuestion" extension = {
	"label" : "Вопросы",
	"control": {
		"template": "/ru/citeck/components/form/controls/table-children.ftl",
		"params": {
			"columns" : "[{key: 'meet_question', label: Alfresco.util.message('form.meetings.planned-issues')},{key: 'meet_plannedReporters_added', label: Alfresco.util.message('form.meetings.planned-reporters'),formatter: Citeck.format.userOrGroup()},{key: 'actions', label: Alfresco.util.message('form.meetings.actions'), formatter: Citeck.format.actionsNonContent({panelID: 'meet-answ-quest-${args.htmlid}'}) }]",
			"responseSchema": "{resultsList: 'props', fields: [{key: 'meet_question'}, {key: 'meet_plannedReporters_added'}, {key: 'nodeRef'}]}",
			"showAddButton" : "${showAddButton}",
			"destNode" : "${args.destination}",
			"showSearchButton" : "true",
			"itemType" : "d:text",
			"itemKey" : "meet_question",
			"itemTitle" : "[[meet_question]]",
			"itemURL" : "Alfresco.constants.PROXY_URI + 'api/citeck/meetings/search-questions?caseNodeRef=${args.destination}&properties=meet:question&replaceColon=_&meetQuestion=[[meet_question]]'",
			"itemURLresults" : "nodes.0",
			"searchURL" : "",
			"rootURL" : "Alfresco.constants.PROXY_URI + 'api/citeck/meetings/search-questions?caseNodeRef=${args.destination}&properties=meet:question&replaceColon=_'",
			"rootURLresults" : "nodes",
			"itemTitle" : "Вопрос: [[meet_question]]"
		}
	}
} />

<@forms.renderField field="prop_meet_answer" extension = extensions.controls.textarea />

</@>
