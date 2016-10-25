<#function tabs view>
	<#assign tabs = [] />
	<#list view.elements as element>
		<#if element.type == "view">
			<#assign tabs = tabs + [ element ] />
		</#if>
	</#list>
	<#return tabs>
</#function>

<#assign tabs = tabs(viewScope.view)>
<#assign bodyTemplate = viewScope.view.params.bodyTemplate!"template-table" />

<ul class="tabs-title">
	<#list tabs as tab>
		<li class="tab-title <#if tab_index == 0>selected</#if>"
			data-tab-id="<#if tab.params.id??>${tab.params.id}<#else>tab-title-${tab_index}</#if>"
			data-bind="click: $root.selectTab, clickBubble: false"
		>
			<#if msg(tab.params.title)?has_content>${msg(tab.params.title)}<#else>${msg("tabs.tab.title")} ${tab_index}</#if>
		</li>
	</#list>
</ul>

<div class="tabs-body ${bodyTemplate}">
	<#list tabs as tab>
		<div class="tab-body <#if tab_index != 0>hidden</#if>" <#if tab.id??>id="${tab.id}-body"</#if>
			 data-body-id="<#if tab.params.id??>${tab.params.id}<#else>tab-body-${tab_index}</#if>"
		>
			<#list tab.elements as element>
				<@views.renderElement element />
			</#list>
		</div>
	</#list>
</div>