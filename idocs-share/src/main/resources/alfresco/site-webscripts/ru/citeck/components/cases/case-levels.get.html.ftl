<#assign el=args.htmlid?js_string />

<@markup id="css" >
	<@link rel="stylesheet" href="${url.context}/res/citeck/components/cases/case-levels.css" group="case-levels" />
</@>

<@markup id="widgets">
	<@inlineScript group="case-levels">
	require(['lib/knockout', 'citeck/utils/knockout.utils', 'citeck/components/cases/case-levels'], function(ko, koutils, caseLevels) {
		YAHOO.util.Event.onContentReady('${el}', function() {
			var Case = koutils.koclass('cases.completeness.Case');
			ko.applyBindings(new Case('${args.nodeRef}'), document.getElementById('${el}'));
			Alfresco.util.createTwister("${el}-heading", "case-levels", {panel: "${el}-body"});
		});
	});
	</@>
</@>

<@markup id="html">
	<#escape x as x?html>
		<div class="case-levels document-details-panel">
			<h2 id="${el}-heading" class="thin dark">
				${msg("header")}
			</h2>
			<div id="${el}-body" class="panel-body">
				<!-- ko foreach: levels -->
				<div class="level" data-bind="css: { completed: completed, current: current, opened: opened }">
					<span class="title" data-bind="text: title, attr: { title: description }, click: toggle"></span>
					<div class="requirements" data-bind="foreach: requirements">
						<div class="requirement" data-bind="css: { passed: passed, opened: opened }">
							<span class="title" data-bind="text: title, attr: { title: description }, click: toggle"></span>
							<div class="matches">
							<!-- ko foreach: matches -->
								<a class="match" data-bind="text: name, attr: { href: '${url.context}/page/card-details?nodeRef=' + nodeRef() } "></a>
							<!-- /ko -->
							<!-- ko if: hasMatches() == false -->
								<div class="message">${msg("no-matches")}</div>
							<!-- /ko -->
							</div>
						</div>
					</div>
				</div>
				<!-- /ko -->
				<!-- ko if: hasLevels() == false -->
					<div class="message">${msg("no-levels")}</div>
				<!-- /ko -->
			</div>
		</div>
	</#escape>
</@>