<#import "journals.lib.ftl" as journals />

<@markup id="css" >
	<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/citeck/components/journals2/journals.css" group="journals2" />
	<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/citeck/components/journals2/journals-dashlet.css" group="journals2" />
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/dynamic-tree/action-renderer.css" group="journals2" />
	<@link rel="stylesheet" href="${page.url.context}/res/yui/calendar/assets/calendar.css" group="journals2" />
	<@link rel="stylesheet" href="${url.context}/res/citeck/utils/citeck.css" group="journals2" />
	<@link type="text/css" href="${url.context}/res/citeck/components/document-journal/document-journal.css" group="journals2" />
</@>

<@markup id="js">
	<@script type="text/javascript" src="${url.context}/res/components/documentlibrary/actions.js" group="journals2" />
	<@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/doclib-actions.js" group="journals2" />
	<@script type="text/javascript" src="${url.context}/res/citeck/components/documentlibrary/custom-actions.js" group="journals2" />
	<@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/error-manager.js" group="journals2" />
	<@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/hierarchy-model.js" group="journals2" />
	<@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/criteria-model.js" group="journals2" />
	<@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/cell-formatters.js?t=523453241" group="journals2" />
	<@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/has-buttons.js" group="journals2" />
	<@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/action-renderer.js" group="journals2" />
	<@script type="text/javascript" src="${url.context}/res/citeck/components/form/constraints.js" group="journals2" />
	<@script type="text/javascript" src="${url.context}/res/modules/journals/buttons.js" group="journals2" />
	<@script type="text/javascript" src="${page.url.context}/res/components/form/date.js" group="journals2" />
</@>

<#assign id = args.htmlid?html />
<#assign toolbarId = id + "-toolbar" />
<#assign pagingOptions = args.pagingOptions!"10,30,50,100" />

<#if config.scoped["ShowStartWorkflowButton"]?? &&
     config.scoped["ShowStartWorkflowButton"].view?? &&
     config.scoped["ShowStartWorkflowButton"].view.attributes["showButton"]??>
        <#assign strValue = config.scoped["ShowStartWorkflowButton"].view.attributes["showButton"]>
        <#if strValue == "false">
            <#assign showWorkflowButton = false>
        <#else>
            <#assign showWorkflowButton = true>
        </#if>
<#else>
    <#assign showWorkflowButton = true>
</#if>

<script type="text/javascript">//<![CDATA[
require(['citeck/components/journals2/journals-dashlet', 'citeck/utils/knockout.yui', 'citeck/utils/knockout.components'], function(JournalsDashlet, koyui, kocomponents) {
//new Alfresco.widget.DashletResizer("${id}", "");
var editEvent = new YAHOO.util.CustomEvent("editJournalsDashlet");
new Alfresco.widget.DashletTitleBarActions("${id}").setOptions({
	actions: [
		<#if isPrivileged>
		{
			cssClass: "edit",
			tooltip: "${msg("dashlet.edit.tooltip")}",
			eventOnClick: editEvent
		},
		</#if>
		{
			cssClass: "help",
			tooltip: "${msg("dashlet.help.tooltip")}",
			bubbleOnClick: {
				message: "${msg("dashlet.help.text")}"
			}
		}
	]
});
var dashlet = new JournalsDashlet("${id}")
.setOptions({
	componentId: "${instance.object.id}",
	model: {
		<@journals.renderCurrentIds />
		dashletConfig: {
			<@journals.renderCurrentIds />
		},
		mode: 'table'
	},
	cache: <@journals.renderCacheJSON />
}).setMessages(${messages});
editEvent.subscribe(dashlet.onEditConfig, dashlet, true);
})
//]]></script>


<div class="dashlet journals-dashlet" data-bind="css: { loading: loading, filtered: filter() != null }">
	<!-- ko if: journalsList() != null -->
    <div class="title" data-bind="text: journalsList().title"></div>
	<!-- /ko -->
	<!-- ko if: journalsList() == null && journal() != null -->
    <div class="title" data-bind="text: journal().title"></div>
	<!-- /ko -->
	<!-- ko if: journalsList() == null && journal() == null -->
    <div class="title">${msg("dashlet.title")}</div>
	<!-- /ko -->
	
    <div class="toolbar flat-button icon-buttons">
	
		<div class="content-message" data-bind="css: { hidden: !(journalsList() == null && journal() == null) }">
			${msg("message.configure-dashlet")}
		</div>

		<!-- journals list -->
		<@journals.renderJournalSelectMenu id />
		
		<!-- create menu -->
		<@journals.renderCreateVariantsMenu id=id hide=true />
		
		<!-- filters list -->
		<@journals.renderFilterSelectMenu id />

		<!-- settings list -->
		<@journals.renderSettingsSelectMenu id />

		<!-- update -->
		<span class="update" title="${msg("button.update")}" data-bind="yuiButton: { type: 'push' }, css: { hidden: journal() == null}">
			<span class="first-child">
				<button data-bind="click: performSearch"></button>
			</span>
		</span>
		
		<@journals.renderCreateReportMenu id />

		<#if (journalsListId!"") == "global-tasks" && showWorkflowButton!true>
			<span data-bind="yuiButton: { type: 'push' }">
				<span class="first-child">
					<a href="/share/page/start-workflow" target="_blank">${msg("button.start-workflow")}</a>
				</span>
			</span>
		</#if>

		<span class="buttons align-right">
		
		<#assign pagingTemplate = '{PreviousPageLink} {CurrentPageReport} {NextPageLink}' />
		<span id="${id}-paging" data-bind="<@journals.renderPaginatorBinding pagingTemplate pagingOptions />" >
		</span>
		
		<span class="fullscreen" title="${msg("button.fullscreen")}" data-bind="yuiButton: {
			type: 'link', 
			href: fullscreenLink()
			}, css: { hidden: journalsList() == null && journal() == null }">
			<span class="first-child">
				<a></a>
			</span>
		</span>
		
		</span>
		
    </div>
    <div class="body">
        <div id="${id}-body">
		
		<div data-bind="css: { hidden: !(mode() == 'table' && journal() != null) }">
			<@journals.renderJournalTable />
		</div>
			
		<div data-bind="css: { hidden: !(mode() == 'table' && journalsList() != null && journal() == null) }">
			<@journals.renderJournalsExpressMenu />
		</div>

		<div class="journals-config" data-bind="css: { hidden: !(mode() == 'config' || journalsList() == null && journal() == null) }">
			<div class="fields" data-bind="with: _dashletConfig">
				<div>
					<label for="${id}-config-listId">${msg("label.journalsList")}</label>
					<select id="${id}-config-listId" data-bind="
						options: $root.journalsLists, 
						value: journalsList, 
						optionsText: 'title',
						optionsCaption: '${msg("message.select-list")}'
					"></select>
				</div>
				<div>
					<label for="${id}-config-journalId">${msg("label.journal")}</label>
					<select id="${id}-config-journalId" data-bind="
						options: $root.resolve('_dashletConfig.journalsList.journals', 'journals', []), 
						value: journal, 
						optionsText: 'title',
						optionsCaption: '${msg("message.select-journal")}'
					"></select>
				</div>
				<div>
					<label for="${id}-config-filterId">${msg("label.filter")}</label>
					<select id="${id}-config-filterId" data-bind="
						options: resolve('journal.type.filters', []), 
						value: filter, 
						optionsText: 'title',
						optionsCaption: '${msg("filter.all")}',
						disable: journal() == null
					"></select>
				</div>
				<div>
					<label for="${id}-config-settingsId">${msg("label.settings")}</label>
					<select id="${id}-config-settingsId" data-bind="
						options: resolve('journal.type.settings', []), 
						value: settings, 
						optionsText: 'title',
						optionsCaption: '${msg("settings.default")}',
						disable: journal() == null
					"></select>
				</div>
			</div>
			<div class="buttons">
				<button data-bind="click: function() { saveConfig(); mode('table'); }">${msg("button.save")}</button>
				<button data-bind="click: function() { mode('table'); }">${msg("button.cancel")}</button>
				<button data-bind="click: resetConfig">${msg("button.reset")}</button>
			</div>
		</div>

        </div>
    </div>
    <div id="${id}-report-form-container" style="display:none;">
        <form id="${id}-report-form" data-bind="attr: {action: createReportLink, target: createReportTarget}, event: {load: createReportFormInit('${id}-report-form')}" enctype="multipart/form-data" method="post">
            <input type="hidden" name="jsondata" data-bind="attr: { value: createReportQuery }" />
        </form>    
    </div>
</div>

<!-- ko gotoAddress: gotoAddress -->
<!-- /ko -->

<!-- ko dependencies: dependencies --><!-- /ko -->
