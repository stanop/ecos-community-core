<#assign el = args.htmlid />

<@markup id="css">
	<@link rel="stylesheet" href="${url.context}/res/citeck/components/type-kind/site-document-types.css" group="type-kind" />
	<@link rel="stylesheet" href="${url.context}/res/components/site/customise-pages.css" group="site"/>
</@>

<@inlineScript group="type-kind">
require(["lib/knockout", "citeck/utils/knockout.utils", "citeck/components/type-kind/site-document-types"], function(ko, koutils, sdt) {
	var Site = koutils.koclass('type-kind.Site');
	var site = new Site({
		name: "${args.site}",
		allTypes: [
			<#list allTypes as type>
			{
				"nodeRef": "${type.nodeRef}",
				"name": "${type.name}"
			}<#if type_has_next>,</#if>
			</#list>
		],
		selectedTypes: [
			<#list selectedTypes as type>
			"${type.nodeRef}"<#if type_has_next>,</#if>
			</#list>
		],
		currentTypes: [
			<#list selectedTypes as type>
			"${type.nodeRef}"<#if type_has_next>,</#if>
			</#list>
		]
	});

	YAHOO.util.Event.onAvailable("${el}-body", function() {
		ko.applyBindings(site, Dom.get("${el}-body"));
	});
});
</@>

<@markup id="html" group="type-kind">
	<div id="${el}-body" class="site-document-types customise-pages">
	
		<div class="available-types">
			<h2>${msg("label.available-types")}</h2>
			<ul data-bind="foreach: availableTypes">
				<li data-bind="text: name, click: $root.addType.bind($root, $data)"></li>
			</ul>
		</div>
		
		<div class="current-types">
			<h2>${msg("label.current-types")}</h2>
			<ul data-bind="foreach: currentTypes">
				<li>
					<div class="type-name" data-bind="text: name"></div>
					<div class="remove" title="${msg("button.remove")}" data-bind="click: $root.removeType.bind($root, $data)"></div>
				</li>
			</ul>
		</div>
		
		<div class="buttons">
			<button id="${el}-ok">${msg("button.ok")}</button>
			<button id="${el}-apply">${msg("button.apply")}</button>
			<button id="${el}-cancel">${msg("button.cancel")}</button>
		</div>
	</div>
</@>


