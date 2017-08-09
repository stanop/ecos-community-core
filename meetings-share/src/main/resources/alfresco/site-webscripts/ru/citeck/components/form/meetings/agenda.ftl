<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields
	fieldNames = [ ]
/>

<#if formUI == "true">
	<@formLib.renderFormsRuntime formId=formId />
</#if>

<#if form.mode == "view">
	<#assign twoColumnClass = "yui-g plain" />
	<#assign threeColumnClass = "yui-gb plain" />
<#else>
	<#assign twoColumnClass = "yui-g" />
	<#assign threeColumnClass = "yui-gb" />
</#if>

<#if form.mode == "create">
	<@forms.formConfirmSupport formId=formId message="" />

	<script type="text/javascript">
		if (Alfresco.CreateContentMgr) {
			Alfresco.CreateContentMgr.prototype.onCreateContentSuccess = function CreateContentMgr_onCreateContentSuccess(response) {
				if (response.json && response.json.persistedObject) {
					// Grab the new nodeRef and pass it on to _navigateForward() to optionally use
					var nodeRef = new Alfresco.util.NodeRef(response.json.persistedObject),
							nextNodeRef = response.json.redirect ? new Alfresco.util.NodeRef(response.json.redirect) : nodeRef;

					if (!this.options.isContainer) {
						Alfresco.Share.postActivity(
							this.options.siteId,
							"org.alfresco.documentlibrary.file-created",
							"{cm:name}", "document-details?nodeRef=" + nodeRef.toString(),
							{ appTool: "documentlibrary", nodeRef: nodeRef.toString() },
							this.bind(function() { this._navigateForward(nextNodeRef); })
						);
					}
				}
			}
		}
	</script>
</#if>

<@formLib.renderFormContainer formId=formId>

<#if form.mode == "create" >
	<input id="alf_assoctype" value="meet:childAgenda" class="hidden" />
	<input id="alf_redirect" value="${args['destination']!''}" class="hidden" />
</#if>

<@forms.renderField field="assoc_meet_plannedParticipants" extension = {
	"control": {
		"template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
		"params": {
			"searchQuery" : "user=true&default=false",
			"defaultUserName": "${(user.id)?js_string}"
		}
	}
} />

<#if form.mode != "view" >
	<#assign showAddButton = "true" />
<#else>
	<#assign showAddButton = "false" />
</#if>

<#if showAddButton == "true" >
	<script type="text/javascript">//<![CDATA[
		(function(){
			YAHOO.Bubbling.on("dynamicTableReady", function(layer, args) {
				var cqFormId = "${args.htmlid}_assoc_meet_childQuestions-cntrl";
				var ppFormId = "${args.htmlid}_assoc_meet_plannedParticipants-cntrl";
				if (args && args[1] && args[1].eventGroup && cqFormId == args[1].eventGroup.id) {
					var childQuestions = Alfresco.util.ComponentManager.get(cqFormId);
					var plannedParticipants = Alfresco.util.ComponentManager.get(ppFormId);
					if (childQuestions && plannedParticipants) {
						function onQuestionAdded(args) {
							var root = plannedParticipants.model.getItem("selected-items");
							var item = args.item;
							if (item && root && item.meet_plannedReporters_added && item.meet_plannedReporters_added.length) {
								for (var i = 0; i < item.meet_plannedReporters_added.length; i++) {
									var newItem = {};
									newItem['_item_name_'] = item.meet_plannedReporters_added[i];
									newItem['nodeRef'] = item.meet_plannedReporters_added[i];
									plannedParticipants.model.addItem(newItem, root, false);
								}
							}
						}
						function onQuestionRemoved(args) {
							var root = plannedParticipants.model.getItem("selected-items");
							var item = args.item;
							if (item && root && item.meet_plannedReporters_added && item.meet_plannedReporters_added.length) {
								for (var i = 0; i < item.meet_plannedReporters_added.length; i++) {
									var existedNumber = 0;
									for (var j in childQuestions.model.items) {
										if (!childQuestions.model.items.hasOwnProperty(j) ||
												childQuestions.model.items[j].selected === 'no' ||
												!childQuestions.model.items[j].meet_plannedReporters_added ||
												!childQuestions.model.items[j].meet_plannedReporters_added.length)
											continue;
										var tableItemReporters = childQuestions.model.items[j].meet_plannedReporters_added;
										for (var k = 0; k < tableItemReporters.length; k++) {
											if (tableItemReporters[k] == item.meet_plannedReporters_added[i])
												existedNumber++;
										}
									}
									if (existedNumber == 0) {
										plannedParticipants.model.deleteItem('authority-' + item.meet_plannedReporters_added[i], root, true);
									}
								}
							}
						}
						// taken from stackoverflow
						function diff1 (o, n) {
							// deal with empty lists
							if (o == undefined) o = [];
							if (n == undefined) n = [];

							// sort both arrays (or this won't work)
							o.sort(); n.sort();

							// don't compare if either list is empty
							if (o.length == 0 || n.length == 0) return {added: n, removed: o};

							// declare temporary variables
							var op = 0; var np = 0;
							var a = []; var r = [];

							// compare arrays and add to add or remove lists
							while (op < o.length && np < n.length) {
								if (o[op] < n[np]) {
									// push to diff?
									r.push(o[op]);
									op++;
								}
								else if (o[op] > n[np]) {
									// push to diff?
									a.push(n[np]);
									np++;
								}
								else {
									op++;np++;
								}
							}

							// add remaining items
							if( np < n.length )
								a = a.concat(n.slice(np, n.length));
							if( op < o.length )
								r = r.concat(o.slice(op, o.length));

							return {added: a, removed: r};
						}
						function onQuestionUpdated(args) {
							if (args.item && args.oldItem) {
								var res = diff1(args.oldItem.meet_plannedReporters_added, args.item.meet_plannedReporters_added);
								if (res.added.length) {
									args.item.meet_plannedReporters_added = res.added;
									onQuestionAdded(args);
								}
								if (res.removed.length) {
									args.item.meet_plannedReporters_added = res.removed;
									onQuestionRemoved(args);
								}
							}
						}
						childQuestions.model.subscribe("childAdded", onQuestionAdded, this, true);
						childQuestions.model.subscribe("childDeleted", onQuestionRemoved, this, true);
						childQuestions.model.subscribe("itemUpdated", onQuestionUpdated, this, true);
					}
				}
			});
		})();
	//]]></script>

	<@forms.renderField field="assoc_meet_childQuestions" extension = {
		"control": {
			"template": "/ru/citeck/components/form/controls/table-children.ftl",
			"params": {
				"columns" : "[{key: 'meet_question', label: Alfresco.util.message('form.meetings.planned-issues')},{key: 'meet_plannedReporters_added', label: Alfresco.util.message('form.meetings.planned-reporters'),formatter: Citeck.format.userOrGroup()},{key: 'actions', label: Alfresco.util.message('form.meetings.actions'), formatter: Citeck.format.actionsNonContent({panelID: 'meetings-agenda-${args.htmlid}'}) }]",
				"responseSchema": "{resultsList: 'props', fields: [{key: 'meet_question'}, {key: 'meet_plannedReporters_added'}, {key: 'nodeRef'}]}",
				"assocType" : "meet:childQuestions",
				"destNode" : "workspace://SpacesStore/attachments-folder-root",
				"showAddButton" : "${showAddButton}"
			}
		}
	} />
<#else>
	<@forms.renderField field="assoc_meet_childQuestions" extension = {
		"control": {
			"template": "/ru/citeck/components/form/controls/table-children.ftl",
			"params": {
				"columns" : "[{key: 'meet_question', label: Alfresco.util.message('form.meetings.planned-issues')},{key: 'meet_plannedReporters_added', label: Alfresco.util.message('form.meetings.planned-reporters'),formatter: Citeck.format.userOrGroup()}]",
				"responseSchema": "{resultsList: 'props', fields: [{key: 'meet_question'}, {key: 'meet_plannedReporters_added'}, {key: 'nodeRef'}]}",
				"assocType" : "meet:childQuestions",
				"destNode" : "workspace://SpacesStore/attachments-folder-root",
				"showAddButton" : "${showAddButton}"
			}
		}
	} />
</#if>

</@>
