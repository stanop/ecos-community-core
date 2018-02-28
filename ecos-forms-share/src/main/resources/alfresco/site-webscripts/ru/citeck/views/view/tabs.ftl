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
	<span class="scroll-tabs scroll-left" data-bind="click: $root.scroll"> << </span>
	<ul>
		<#list tabs as tab>
			<#assign tabId = tab.id!tab.params.setId />

			<!-- ko with: $root.node().impl().getAttributeSet("${tabId}") -->
				<li class="tab-title" data-tab-id="${tabId}" id="${tabId}"
					data-bind="
						css: { selected: selected, hidden: irrelevant, disabled: disabled }, 
						click: _.bind($root.selectAttributeSet, $root), clickBubble: false
					"
				>
					<#if msg(tab.params.title)?has_content>
						${msg(tab.params.title)}
					<#else>
						${msg("tabs.tab.title")} ${tab_index}
					</#if>

					
					<!-- ko if: invalid -->
						<i class="fa fa-exclamation-circle warning" aria-hidden="true"></i>
					<!-- /ko -->
				</li>
			<!-- /ko -->
		</#list>
	</ul>
	<span class="scroll-tabs scroll-right" data-bind="click: $root.scroll"> >> </span>
</div>

<div class="tabs-body ">
	<#list tabs as tab>
		<#assign tabId = tab.id!tab.params.setId />

         <#if tab.params.warningMessage??>
             <!-- ko component: { name: "free-content", params: {
                         func: ko.computed(function() { ${tab.params.warningMessage} })
             }} --><!-- /ko -->
        </#if>
		<!-- ko with: $root.node().impl().getAttributeSet("${tabId}") -->
			<!-- ko if: _rendered -->
				<div class="tab-body" id="${args.htmlid}-${tabId}" data-tab-id="${tabId}" 
					data-bind="css: { hidden: !selected() }"
				>
					<!-- ko with: $root.node().impl() -->
						<@views.renderElement tab />
					<!-- /ko -->
				</div>
			<!-- /ko -->
		<!-- /ko -->
	</#list>
</div>