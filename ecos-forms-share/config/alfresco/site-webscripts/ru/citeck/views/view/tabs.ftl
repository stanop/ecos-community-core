<#assign bodyTemplate = viewScope.view.params.bodyTemplate!"template-table" />

<div class="tabs-title">
	<span class="scroll-tabs scroll-left hidden" data-bind="click: $root.scrollTabs"> << </span>
	<ul>
		<#list groups as tab>
			<#assign tabId = tab.id!tab.genId + "-" + tab_index />
			<li class="tab-title <#if tab_index == 0>selected</#if>"
				data-tab-id="${tabId}"
				data-tab-index="${tab_index}"
				data-bind="click: $root.selectTab, clickBubble: false"
			>
				<#if msg(tab.params.title)?has_content>${msg(tab.params.title)}<#else>${msg("tabs.tab.title")} ${tab_index}</#if>
			</li>
		</#list>
	</ul>
	<span class="scroll-tabs scroll-right hidden" data-bind="click: $root.scrollTabs"> >> </span>
</div>

<div class="tabs-body ${bodyTemplate}">
	<#list groups as tab>
		<#assign tabId = tab.id!tab.genId + "-" + tab_index />
		<div class="tab-body <#if tab_index != 0>hidden</#if>"
			 id="${args.htmlid}-${tabId}"
			 data-tab-id="${tabId}"
		>
			<#list tab.elements as element>
				<@views.renderElement element />
			</#list>
		</div>
	</#list>
</div>