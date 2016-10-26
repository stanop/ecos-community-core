<#function getTabs view>
	<#assign tabs = [] />
	<#list view.elements as element>
		<#if element.type == "view">
			<#assign tabs = tabs + [ element ] />
		</#if>
	</#list>
	<#return tabs>
</#function>

<#function getTabId tab index>
	<#assign tabid = "tab" />
	<#if viewScope.view.id?has_content><#assign tabid = tabid + "-" + viewScope.view.id /></#if>
	<#if tab.id?has_content><#assign tabid = tabid + "-" + tab.id /></#if>
	<#assign tabid = tabid + "-" + index />
	<#return tabid>
</#function>

<#function getIds tabs>
	<#assign tabsids = [] />
	<#list tabs as tab>
		<#assign tabsids = tabsids + [ { "data": tab, "id": getTabId(tab, tab_index) } ] />
	</#list>
	<#return tabsids>
</#function>

<#assign tabs = getTabs(viewScope.view) />
<#assign tabids = getIds(tabs) />
<#assign bodyTemplate = viewScope.view.params.bodyTemplate!"template-table" />

<ul class="tabs-title">
	<#list tabids as tab>
		<li class="tab-title <#if tab_index == 0>selected</#if>"
			data-tab-id="${tab.id}"
			data-bind="click: $root.selectTab, clickBubble: false"
		>
			<#if msg(tab.data.params.title)?has_content>${msg(tab.data.params.title)}<#else>${msg("tabs.tab.title")} ${tab_index}</#if>
		</li>
	</#list>
</ul>

<div class="tabs-body ${bodyTemplate}">
	<#list tabids as tab>
		<div class="tab-body <#if tab_index != 0>hidden</#if>" <#if tab.data.id??>id="${tab.data.id}-body"</#if>
			 data-tab-id="${tab.id}"
		>
			<#list tab.data.elements as element>
				<@views.renderElement element />
			</#list>
		</div>
	</#list>
</div>