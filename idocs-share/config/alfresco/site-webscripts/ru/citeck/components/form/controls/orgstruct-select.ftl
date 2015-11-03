<#include "common/orgstruct-picker.inc.ftl" />
<#assign controlId = fieldHtmlId + "-cntrl">

<#-- choose selectable types -->
<#if field.control.params.selectable??>
	<#-- if it is explicitly specified -->
	<#assign selectable = field.control.params.selectable?split(",") />
<#else>
	<#-- if it is not specified, rely on association target type -->
	<#assign type2selectable = {
		"cm:person": [ "USER" ],
		"cm:authorityContainer": [ "GROUP-role" ],
		"cm:authority": [ "GROUP-role", "USER" ],
		"sys:base": [ "GROUP", "USER" ]
	} />
	<#-- if it is not cm:authority, we can not select anything -->
	<#assign selectable = type2selectable[field.endpointType]![] />
</#if>

<#if field.control.params.defaultUserName??>
	<#assign defaultUserName = field.control.params.defaultUserName />
<#else />
	<#assign defaultUserName = "" />
</#if>

<script type="text/javascript">//<![CDATA[
(function() {
	<@renderDynamicTreePickerJS field "picker" field.control.params.valueField!"nodeRef" />
	var model = <@renderOrgstructModelJS params = field.control.params!{} />;
	
	picker.setOptions({
		model: model,
		tree: {
			sorting: {
				"": [
					{ by: "{authorityType}" }, // GROUP, USER
					{ by: "{groupType}", descend: true }, // role, branch, <none>
					{ by: "{roleIsManager}", descend: true }, // managers first
					{ by: "{firstName}-{lastName}-{displayName}" } // firstName/lastName for users, displayName for groups
				],
			},
			buttons: {
				<#list selectable as selectableKey>
				"${selectableKey}": [ "itemSelect" ],
				</#list>
				"selected-yes": [ "itemUnselect" ],
			},
		},
		list: {
			sorting: {
				"selected-items": [
					{ by: "{selected-index}" }
				]
			},
			buttons: {
				"selected-yes": [ "itemUnselect" ],
			},
		},
	});
	
	<#-- TODO: refactor this -->
	<#if form.mode == "create" && defaultUserName != "">
	YAHOO.Bubbling.on("objectFinderReady", function(layer, args) {
		var userName = "${defaultUserName}";
		if(!this.model || !this.model.getItem("selected-items") || !this.model.getItem("selected-items")._item_children_|| this.model.getItem("selected-items")._item_children_.length==0)
		{
			var finder = args[1].eventGroup;
			if (finder.id === this.id) {
				var searchUrl = Alfresco.constants.PROXY_URI + "api/orgstruct/authority/"+userName;
				var request = new XMLHttpRequest();
				request.open('GET', searchUrl, false);  // `false` makes the request synchronous
				request.send(null);
				if (request.status === 200) {
					if (request.responseText)
					{
						var data = eval('(' + request.responseText + ')');
						data.selected = "yes";
						this.widgets.dialog.fireEvent("itemsSelected", [].concat([data]));
						//this.widgets.dialog.fireEvent("onItemSelect", this);
						//.model.getItem("selected-items")._item_children_ = [data];
						//picker.widgets.dialog.model.updateChildren("selected-items", true);
						
					}
				}
			}
		}
		else
		{
			var items = this.model.getItem("selected-items")._item_children_;
			for(var i = 0; i < items.length; i++)
			items[i].selected = "yes";
		}
	}, picker);
	</#if>
	
	
})();
//]]></script>

<@renderDynamicTreePickerControlHTML field />
