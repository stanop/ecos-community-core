<#import "journals.lib.ftl" as journals />

<#macro renderJournalsPickerJS htmlid elementType>
<#assign pagingOptions = args.pagingOptions!"10" />
(function() {
	return new Citeck.widget.JournalsPicker("${htmlid}")
	.setOptions(<@journals.renderCacheJSON />)
	.setOptions({
		predicateLists: <@journals.renderPredicateListsJSON />,
		model: <@journals.renderCriteriaModelJSON />,
		pagingOptions: [${pagingOptions}],
		type: "${elementType}"
	}).setMessages(${messages});
})()
</#macro>

<#macro renderJournalsPickerHTML id>

<script type="html/template" id="loading-template">
<img src="${url.context}/res/citeck/components/journals2/images/loading.gif" /> ${msg('label.loading')}
</script>

<div id="${id}" class="journals-picker" data-bind="yuiPanel: { 
		visible: visible,
		modal: true,
		close: true,
		fixedcenter: true,
		zIndex: 1000,
		width: '820px'
	},
	css: {
		loading: loading, 
		filtered: criteria().length != 0
	}" class="journals-config">
	<div class="hd">${msg("form.select.label")}</div>
	<div class="bd">
	
		<div class="toolbar flat-button icon-buttons">
			
			<span class="filter" data-bind="yuiButton: { type: 'checkbox', checked: currentPanel() == 'filter' }">
				<span class="first-child">
					<button data-bind="click: currentPanel.bind($data, 'filter')">${msg("button.search")}</button>
				</span>
			</span>
			
			<!-- ko if: selectedCreateVariant -->
			<span class="create" data-bind="yuiButton: { type: 'checkbox', checked: currentPanel() == 'create' }">
				<span class="first-child">
					<button data-bind="click: currentPanel.bind($data, 'create')">${msg("button.create")}</button>
				</span>
			</span>
			<!-- /ko -->

			<span style="float: right">
			
			<!-- ko if: journal -->
			
				<!-- ko if: currentPanel() == 'filter' -->
					<!-- ko if: paging() != null -->
						<#assign pagingTemplate = '{PreviousPageLink} {CurrentPageReport} {NextPageLink}' />
						<span id="${id}-paging" data-bind="<@journals.renderPaginatorBinding pagingTemplate pagingOptions />" >
						</span>
					<!-- /ko -->
						
					<span class="update" title="${msg("button.update")}" data-bind="yuiButton: { type: 'push' }">
						<span class="first-child">
							<button data-bind="click: performNewSearch"></button>
						</span>
					</span>
					
				<!-- /ko -->
			<!-- /ko -->
			
			<@journals.renderJournalSelectMenu id 1 />
				
			</span>
		</div>

		<div class="toolbar-menu filter" data-bind="css: { hidden: currentPanel() != 'filter' }">

			<div class="panel-body">
				<@journals.renderJournalTable />
			</div>
			
		</div>
	
		<div class="toolbar-menu create" data-bind="css: { hidden: currentPanel() != 'create' }">
			<!-- ko ifnot: createVariant -->
			WARN: create variant is not selected
			<!-- /ko -->
			<!-- ko if: createVariant -->
				<h2 data-bind="text: createVariant().title"></h2>
				
				<div data-bind="template: { name: createFormTemplate() || 'loading-template' }"></div>
				
				<!-- ko with: createVariant -->
				<div class="hidden" data-bind="
					templateSetter: {
						name: $root.createFormTemplate, 
						url: '${url.context}/page/components/form?htmlid=${id}-create-form&itemKind=type&itemId=' + type() + '&formId=' + (formId()||'') + '&destination=' + destination() + '&mode=create&submitType=json&showResetButton=true&showCancelButton=false'
					}">
				</div>
				<!-- /ko -->
				
			<!-- /ko -->
		</div>
	
		<div class="panel selected-items">
			<label>selected items</label>
			<div class="items">
				<!-- ko foreach: _selectedIds -->
				<span class="item">
					<span class="name" data-bind="text: $data"></span>
				</span>
				<!-- /ko -->
				<br />
				<!-- ko foreach: _selectedRecords -->
				<span class="item">
					<a class="remove" title="${msg("button.remove-item")}" data-bind="click: $root._selectedIds.remove.bind($root._selectedIds, $data.nodeRef)"></a>
					<span class="name" data-bind="text: $data.attributes['cm:name']"></span>
				</span>
				<!-- /ko -->
			</div>
		</div>
		
	</div>
	<div class="ft">
		<button type="button" data-bind="click: function() { select(_selectedIds()); hide(); }">${msg("button.ok")}</button>
		<button type="button" data-bind="click: hide">${msg("button.cancel")}</button>
	</div>
</div>
</#macro>