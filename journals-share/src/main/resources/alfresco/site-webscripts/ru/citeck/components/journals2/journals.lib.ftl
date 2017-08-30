<#macro renderCurrentIds>
	journalsList: <#if journalsListId??>"${journalsListId}"<#else>null</#if>,
	journal: <#if journalId??>"${journalId}"<#else>null</#if>,
	filter: <#if filterId??>"${filterId}"<#else>null</#if>,
	settings: <#if settingsId??>"${settingsId}"<#else>null</#if>,
	actionGroup: <#if actionGroupId??>"${actionGroupId}"<#else>"none"</#if>,
</#macro>

<#macro renderCacheJSON>
{
	<#if journalsListId??>
	JournalsList: [ ${journalsListJSON} ],
	</#if>
	<#if journalId??>
	Journal: [ ${journalJSON} ],
	</#if>
	<#if journalType??>
	JournalType: [
		{
			"id": "${journalType}",
			"filters": ${filtersListJSON},
			"settings": ${settingsListJSON}
		}
	],
	</#if>
	<#if filterId??>
	Filter: [ ${filterJSON} ],
	</#if>
	<#if settingsId??>
	Settings: [ ${settingsJSON} ],
	</#if>
	<#if (outputPredicates!false) == true>
	<#assign predicateLists = config.scoped["Search"]["predicate-lists"] />
	PredicateList: [
		<#list predicateLists.children as predicateList>
		{
			"id": "${predicateList.attributes["id"]}",
			"predicates": [
			<#list predicateList.childrenMap["predicate"]![] as predicate>
			{
				"id": "${predicate.value}",
				"label": "${msg(predicate.attributes["label"]!"predicate." + predicate.value)}",
				"needsValue": ${predicate.attributes["needsValue"]!"true"}
			}<#if predicate_has_next>,</#if>
			</#list>
			]
		}<#if predicateList_has_next>,</#if>
		</#list>
	],
	Datatype: [
		<#list predicateLists.children as predicateList>
		<#list predicateList.childrenMap["type"]![] as type>
		{
			"name": "${type.value}",
			"predicateList": "${predicateList.attributes['id']}"
		}<#if predicateList_has_next || type_has_next>,</#if>
		</#list>		
		</#list>		
	],
	</#if>
	Attribute: [
		{
			name: "type",
			displayName: "${msg("label.subtype")}",
			datatype: "text",
			labels: {}
		},
		{
			name: "filesize",
			displayName: "${msg("label.size")}",
			datatype: "filesize"
		},
		{
			name: "mimetype",
			displayName: "${msg("label.mimetype")}",
			datatype: "mimetype"
		},
	],
}
</#macro>

<#macro renderMultiActionsJSON>
<#escape x as x?js_string>
[
	<#assign multiActions = config.scoped["DocumentLibrary"]["multi-select"] />
	<#list multiActions.children as action>
	{
		"id": "${action.attributes.id}",
		"func": "${action.attributes.function!action.attributes.id}",
		"label": "${msg(action.attributes.label)}",
		"isDoclib": ${msg(action.attributes.isDoclib!"true")},
		"permission": <#if action.attributes.permission??>"${action.attributes.permission}"<#else>null</#if>,
		"requiredAspect": <#if action.attributes.hasAspect??>"${action.attributes.hasAspect}"<#else>null</#if>,
		"forbiddenAspect": <#if action.attributes.notAspect??>"${action.attributes.notAspect}"<#else>null</#if>,
		"syncMode": <#if action.attributes.syncMode??>"${action.attributes.syncMode}"<#else>null</#if>,
		"settings": <@renderActionOptions options=action.childrenMap['param']![] />
	}<#if action_has_next>,</#if>
	</#list>
]
</#escape>
</#macro>

<#macro renderActionOptions options>
{
	<#list options as option>
		"${option.attributes.name}": "${option.value}"<#if option_has_next>,</#if>
	</#list>
}
</#macro>

<#macro renderJournalTable>
	<script type="text/javascript">//<![CDATA[
	var longUniqueNameOfColumnTemplate = function(c) {
		return {
			key: c.key(),
			label: c.label() || '',
			sortable: c.sortable(),
			formatter: this.getFormatter(c)
		};
	};
	var longUniqueNameOfSettings = {
		'MSG_EMPTY': '${msg('message.dynamic-table.empty')?html}',
		'MSG_ERROR': '${msg('message.dynamic-table.error')?html}',
		'MSG_LOADING': '${msg('message.dynamic-table.loading')?html}',
		'MSG_SORTASC': '${msg('message.dynamic-table.sortasc')?html}',
		'MSG_SORTDESC': '${msg('message.dynamic-table.sortdesc')?html}',
	};
	//]]></script>
	<div class="journal-content-table content-table hide-buttons" data-bind="yuiDataTable: {
        fields: fields,
		columns: columns,
		records: records,
		sortedBy: sortBy,
		columnTemplate: longUniqueNameOfColumnTemplate,
		set: longUniqueNameOfSettings,
		sortedBy: sortByFirst,
		doubleClickConfig: {
			field: recordIdField,
			setter: selectedId
		}
	}">
	</div>
</#macro>

<#macro renderPaginatorBinding pagingTemplate pagingOptions>
	css: { hidden: journal() == null },
	yuiPaginator: {
		recordOffset: skipCount,
		rowsPerPage : maxItems,
		totalRecords: totalEstimate,
		template: '${pagingTemplate}',
		previousPageLinkLabel: '${msg('pagination.previousPageLinkLabel')?html}',
		previousPageLinkTitle: '${msg('pagination.previousPageLinkTitle')?html}',
		nextPageLinkLabel: '${msg('pagination.nextPageLinkLabel')?html}',
		nextPageLinkTitle: '${msg('pagination.nextPageLinkTitle')?html}',
		rowsPerPageOptions: [${pagingOptions}],
		pageReportTemplate: '{startRecord} - {endRecord}',
		pageLabelBuilder: function (page, paginator) {
			var start = paginator.getPageRecords(page)[0];
			return YAHOO.lang.substitute('{0} - {1}', { 
				'0': start + 1, 
				'1': start + paginator.getRowsPerPage()
			});
		},
		pageTitleBuilder: function (page, paginator) {
			var start = paginator.getPageRecords(page)[0];
			return YAHOO.lang.substitute('${msg('pagination.pageReportTitle')?js_string}', { 
				'0': start + 1, 
				'1': start + paginator.getRowsPerPage()
			});
		}
	}
</#macro>

<#macro renderJournalsExpressMenu>
	<div class="content-message">
		${msg("message.select-journal")}
	</div>
	<div class="journals-menu" data-bind="foreach: resolve('journalsList.journals', [])">
		<span class="journal" data-bind="text: title, click: $root.journal"></span>
	</div>
</#macro>

<#macro renderJournalSelectMenu id minJournals = 0>
	<!-- ko if: journalsList() != null && journalsList().journals().length > ${minJournals?c} -->
	<span class="journal-select" title="${msg("button.select-journal")}" data-bind="yuiButton: { type: 'menu', menu: '${id}-journals-menu' }">
		<span class="first-child">
			<button data-bind="text: journal() ? journal().title() : '${msg("message.select-journal")}'"></button>
		</span>
	</span>
	<div id="${id}-journals-menu" class="yui-overlay yuimenu button-menu">
		<div class="bd">
			<ul data-bind="foreach: journalsList().journals()" class="first-of-type">
				<li class="yuimenuitem">
					<a class="yuimenuitemlabel" data-bind="text: title, click: $root.selectJournal.bind($root, $data.nodeRef())"></a>
				</li>
			</ul>
		</div>
	</div>
	<!-- /ko -->
</#macro>

<#macro renderFilterSelectMenu id>
	<!-- ko if: filters().length > 0 -->
	<span class="filter-select" title="${msg("button.select-filter")}" data-bind="yuiButton: { type: 'menu', menu: '${id}-filters-menu' }">
		<span class="first-child">
			<button data-bind="text: filter() ? filter().title() : '${msg("message.select-filter")}'"></button>
		</span>
	</span>
	<div id="${id}-filters-menu" class="yui-overlay yuimenu button-menu">
		<div class="bd">
			<ul class="first-of-type">
				<li class="yuimenuitem">
					<a class="yuimenuitemlabel" data-bind="click: $root.selectFilter.bind($root, '')">${msg("filter.all")}</a>
				</li>
				<!-- ko foreach: filters -->
				<li class="yuimenuitem">
					<a class="yuimenuitemlabel" data-bind="text: title, click: $root.selectFilter.bind($root, $data.nodeRef())"></a>
				</li>
				<!-- /ko -->
			</ul>
		</div>
	</div>
	<!-- /ko -->
</#macro>

<#macro renderSettingsSelectMenu id>
	<!-- ko if: settingsList().length > 0 -->
	<span class="settings-select" title="${msg("button.select-settings")}" data-bind="yuiButton: { type: 'menu', menu: '${id}-settings-menu' }">
		<span class="first-child">
			<button data-bind="text: settings() ? settings().title() : '${msg("message.select-settings")}'"></button>
		</span>
	</span>
	<div id="${id}-settings-menu" class="yui-overlay yuimenu button-menu">
		<div class="bd">
			<ul class="first-of-type">
				<li class="yuimenuitem">
					<a class="yuimenuitemlabel" data-bind="click: $root.selectSettings.bind($root, '')">${msg("settings.default")}</a>
				</li>
				<!-- ko foreach: settingsList -->
				<li class="yuimenuitem">
					<a class="yuimenuitemlabel" data-bind="text: title, click: $root.selectSettings.bind($root, $data.nodeRef())"></a>
				</li>
				<!-- /ko -->
			</ul>
		</div>
	</div>
	<!-- /ko -->
</#macro>

<#macro renderCreateVariantsMenu id icon=false hide=false>
	<#assign toolbarId = id + "-toolbar" />
	<span class="create" title="${msg("button.create.tip")}" data-bind="yuiButton: { type: 'menu', <#if !hide>disabled: resolve('journal.availableCreateVariants.length', 0) == 0,</#if> menu: '${toolbarId}-create-menu' }, <#if hide>css: { hidden: resolve('journal.availableCreateVariants.length', 0) == 0 }</#if>">
		<span class="first-child">
			<button><#if !icon>${msg("button.create")}</#if></button>
		</span>
	</span>
	<div id="${toolbarId}-create-menu" class="yui-overlay yuimenu button-menu">
		<div class="bd">
			<ul data-bind="foreach: resolve('journal.availableCreateVariants', [])" class="first-of-type">
				<li class="yuimenuitem">
					<a class="yuimenuitemlabel" data-bind="text: title, attr: { href: link() }, css: { 'default-create-variant': isDefault }"></a>
				</li>
			</ul>
		</div>
	</div>
</#macro>

<#macro renderCreateReportMenu id icon=false hide=false>
    <#assign toolbarId = id + "-toolbar" />
    <span class="report" title="${msg("button.report.tip")}" data-bind="yuiButton: { type: 'menu', <#if !hide>disabled: reportButtonDisabled(),</#if> menu: '${toolbarId}-report-menu' }, <#if hide>css: { hidden: resolve('journal.availableCreateVariants.length', 0) == 0 }</#if>">
        <span class="first-child">
            <button><#if !icon>${msg("button.report")}</#if></button>
        </span>
    </span>
    <div id="${toolbarId}-report-menu" class="yui-overlay yuimenu button-menu">
        <div class="bd">
            <ul class="first-of-type">
                <li class="yuimenuitem">
                    <a class="yuimenuitemlabel" data-bind="click: createReport.bind($data, 'html', false)">${msg("report.html.view")}</a>
                </li>
                <li class="yuimenuitem">
                    <a class="yuimenuitemlabel" data-bind="click: createReport.bind($data, 'html', true)">${msg("report.html.download")}</a>
                </li>
                <#-- <li class="yuimenuitem">
                    <a class="yuimenuitemlabel" data-bind="click: createReport.bind($data, 'pdf', true)">PDF</a>
                </li> -->
                <li class="yuimenuitem">
                    <a class="yuimenuitemlabel" data-bind="click: createReport.bind($data, 'xlsx', true)">Excel</a>
                </li>    
            </ul>
        </div>
    </div>
</#macro>