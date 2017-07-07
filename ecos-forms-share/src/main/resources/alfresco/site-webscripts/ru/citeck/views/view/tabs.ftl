<#function getTabs view>
	<#assign tabs = [] />
	<#list view.elements as element>
		<#if element.type == "view">
			<#assign tabs = tabs + [ element ] />
		</#if>
	</#list>
	<#return tabs>
</#function>

<#assign tabs = getTabs(viewScope.view) />

<div class="tabs-title">
	<span class="scroll-tabs scroll-left hidden" data-bind="click: $root.scrollGroups"> << </span>
	<ul>
		<#list tabs as tab>
			<#assign tabId = tab.id!tab.genId />
			<!-- ko with: group("${tabId}") -->
				<li class="tab-title <#if tab_index == 0>selected</#if>"
					data-tab-id="${tabId}"
					data-tab-index="${tab_index}"
					data-bind="click: _.bind($root.selectGroup, $root), clickBubble: false"
				>
					<#if msg(tab.params.title)?has_content>${msg(tab.params.title)}<#else>${msg("tabs.tab.title")} ${tab_index}</#if>
					<!-- ko if: invalid -->
						<i class="fa fa-exclamation-circle" aria-hidden="true"></i>
					<!-- /ko -->
				</li>
			<!-- /ko -->
		</#list>
	</ul>
	<span class="scroll-tabs scroll-right hidden" data-bind="click: $root.scrollGroups"> >> </span>
</div>

<div class="tabs-body ">
	<#list tabs as tab>
		<#assign tabId = tab.id!tab.genId />
		<div class="tab-body <#if tab_index != 0>hidden</#if>"
			 id="${args.htmlid}-${tabId}"
			 data-tab-id="${tabId}"
		>
			<@views.renderElement tab />
		</div>
	</#list>
</div>