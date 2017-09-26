<#assign el = args.htmlid />

<@markup id="css">
	<@link rel="stylesheet" href="${url.context}/res/citeck/components/type-kind/site-document-types.css" group="type-kind" />
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
				"name": "${type.name}",
				"title": "${type.title}"
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
	
	var exit = function() {
		window.location = Alfresco.util.siteURL('dashboard');
	}

	YAHOO.util.Event.onAvailable("${el}-body", function() {
		ko.applyBindings({
			site: site,
			save: function(callback) {
				site.save();
				exit();
			},
			apply: function() {
				site.save();
			},
			cancel: exit
		}, Dom.get("${el}-body"));
	});
});
</@>

<@markup id="html" group="type-kind">
	<div id="${el}-body" class="site-document-types" data-bind="with: site">
		<p class="help">${msg("help.description")}</p>
	
		<div class="available-types">
			<h2>${msg("label.available-types")}</h2>
			<ul data-bind="foreach: availableTypes">
				<li data-bind="text: name, click: $parent.addType.bind($parent, $data)"></li>
			</ul>
		</div>
		
		<div class="current-types">
			<h2>${msg("label.current-types")}</h2>
			<ul data-bind="foreach: currentTypes">
				<li>
					<div class="type-name" data-bind="text: name"></div>
					<div class="remove" title="${msg("button.remove")}" data-bind="click: $parent.removeType.bind($parent, $data)"></div>
				</li>
			</ul>
			<p class="help" data-bind="visible: currentTypes().length == 0">${msg("help.no-types-selected")}</p>
		</div>
		
		<div class="buttons">
			<span class="yui-button">
				<span class="first-child">
					<button id="${el}-ok" data-bind="click: $root.save">${msg("button.ok")}</button>
				</span>
			</span>
			<span class="yui-button">
				<span class="first-child">
					<button id="${el}-apply" data-bind="click: $root.apply">${msg("button.apply")}</button>
				</span>
			</span>
			<span class="yui-button">
				<span class="first-child">
					<button id="${el}-cancel" data-bind="click: $root.cancel">${msg("button.cancel")}</button>
				</span>
			</span>
		</div>
	</div>
</@>


