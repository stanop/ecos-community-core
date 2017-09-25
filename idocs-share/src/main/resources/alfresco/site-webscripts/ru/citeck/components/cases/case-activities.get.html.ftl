<#assign el=args.htmlid?js_string />

<@markup id="css" >
	<@link rel="stylesheet" href="${url.context}/res/citeck/components/cases/case-activities.css" group="case-activities" />
</@>

<@markup id="widgets">
	<@inlineScript group="case-activities">
		require(['lib/knockout', 'citeck/utils/knockout.utils', 'citeck/utils/knockout.yui', 'citeck/components/cases/case-activities'], function(ko, koutils, koyui, caseActivities) {
			YAHOO.util.Event.onContentReady('${el}', function() {
				var Activity = koutils.koclass('cases.activities.Activity');
				var activity = new Activity({
					nodeRef: "${args.nodeRef}",
					startable: false,
					stoppable: false,
					editable: true,
					removable: false,
					composite: true
				});
				
				ko.applyBindings(activity, document.getElementById('${el}'));
				Alfresco.util.createTwister("${el}-heading", "case-activities", {panel: "${el}-body"});
				YAHOO.Bubbling.on("metadataRefresh", function() {
					activity.reload(true);
				});
			});
		});
	</@>
</@>

<@markup id="html">
	<#escape x as x?html>
		<script type="html/template" id="add-activity">
			<!-- ko if: composite() && editable() -->
				<div data-bind="attr: { id: nodeRef() + '-activity-create-menu' }" class="yui-overlay yuimenu button-menu" style="visibility: hidden">
					<div class="bd">
						<ul class="first-of-type">
						<#list createMenu as submenu>

							<li class="yuimenuitem">
								<span class="yuimenuitemlabel">${submenu.title}</span>
								<div class="yuimenu" style="visibility: hidden">
									<div class="bd">
										<ul>
										<#list submenu.variants as variant>
											<li class="yuimenuitem">
												<a class="yuimenuitemlabel" data-bind="click: $data.add.bind($data, '${variant.type}', '${variant.formId!}')">${variant.title}</a>
											</li>
										</#list>
										</ul>
									</div>
								</div>
							</li>

						</#list>
						</ul>
					</div>
				</div>
				<span title="${msg("action.create")}" data-bind="yuiButton: { type: 'menu', menu: nodeRef() + '-activity-create-menu' }, click: function() {}, clickBubble: false">
					<span class="first-child action create">
						<button>&nbsp;</button>
					</span>
				</span>
			<!-- /ko -->
		</script>
	
		<script type="html/template" id="activities">
			<ul data-bind="DDTarget: { data: $data }, css: { open: opened }" class="activity-list">
				<!-- ko foreach: activities -->
					<li class="activity" data-bind="css: { opened: opened, composite: composite, started: started, stopped: stopped }, DDList: { index: $index(), data: $data, parent: $parent }">
						<div class="activity-info">
							<div class="left">
								<span class="twister" data-bind="css: { opened: opened }, click: toggle, clickBubble: false" />
								<span class="title" data-bind="text: title, attr: { title: description }, event: { dblclick: function() { console.log($data) } }" />
							</div>
							<div class="right">
								<span class="type" data-bind="text: typeTitle, attr: { title: typeTitle }" />
								<span class="start" data-bind="text: startDateText, attr: { title: startTimeText }" />
								<span class="end" data-bind="text: endDateText, attr: { title: endTimeText }" />
								
								<!-- ko if: startable -->
									<a class="action start" title="${msg("action.start")}" data-bind="click: start, clickBubble: false">&nbsp;</a>
								<!-- /ko -->
								<!-- ko if: stoppable -->
									<a class="action stop" title="${msg("action.stop")}" data-bind="click: stop, clickBubble: false">&nbsp;</a>
								<!-- /ko -->
								
								<!-- ko template: { name: 'add-activity' } --><!-- /ko -->

								<!-- ko if: editable -->
									<a class="action edit" title="${msg("action.edit")}" data-bind="click: edit, clickBubble: false">&nbsp;</a>
								<!-- /ko -->
								<!-- ko if: removable -->
									<a class="action remove" title="${msg("action.remove")}" data-bind="click: $parent.remove.bind($parent, $data), clickBubble: false">&nbsp;</a>
								<!-- /ko -->
							</div>
						</div>
						
						<!-- ko template: 'activities' --><!-- /ko -->
					</li>
				<!-- /ko -->

				<!-- ko ifnot: activities.loaded() -->
					<li class="message loading-message">${msg("loading")}</li>
				<!-- /ko -->

				<!-- ko if: activities.loaded() && !hasActivities() -->
					<li class="message empty-message">${msg("no-activities")}</li>
				<!-- /ko -->
			</ul>
		</script>
	
		<div class="case-activities document-details-panel">
			<h2 id="${el}-heading" class="thin dark">
				${msg("header")}
				<span class="alfresco-twister-actions">
					<!-- ko template: { name: 'add-activity' } --><!-- /ko -->
				</span>
			</h2>

			<div id="${el}-body" class="panel-body">
				<span class="header right" data-bind="visible: hasActivities()">
					<span class="type">${msg('header.type')}</span>
					<span class="start">${msg('header.start')}</span>
					<span class="end">${msg('header.end')}</span>
				</span>
				
				<#assign errorMessage = msg("move.failure")>
				<div id="${el}-DnD" class="dnd-area" data-bind="DDRoot: { data: $data, errorMessage: '${errorMessage}' }">
					<!-- ko template: { name: 'activities' } --><!-- /ko -->	
				</div>			
			</div>
		</div>
	</#escape>
</@>