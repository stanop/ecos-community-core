<#import "orgstruct.lib.ftl" as orgstructlib />

<#assign el=args.htmlid?html>
<#assign rootGroup = "_orgstruct_home_" />
<script type="text/javascript">//<![CDATA[
    new Alfresco.component.ConsoleOrgstruct("${el}").setOptions({
		currentFilter: "orgstruct",
		filters: [
			{
				name: "orgstruct",
				model: {
					formats: {
						"authority": {
							name: "authority-{fullName}",
							keys: [ "{groupType}-manager-{roleIsManager}", "{authorityType}-{groupType}", "{authorityType}", "available-{available}" ]
						}
					},
					item: {
						"GROUP": {
							"format": "authority",
							"get": "${page.url.context}/proxy/alfresco/api/orgstruct/authority/{fullName}",
						},
						"USER": {
							"format": "authority",
							"get": "${page.url.context}/proxy/alfresco/api/orgstruct/authority/{fullName}",
						}
					},
					children: {
						"root": {
							"format": "authority",
							"get": "${page.url.context}/proxy/alfresco/api/orgstruct/group/${rootGroup}/children/",
							"delete": "${page.url.context}/proxy/alfresco/api/groups/${rootGroup}/children/{item.fullName}",
						},
						"search": {
							"format": "authority",
							"get": "${page.url.context}/proxy/alfresco/api/orgstruct/group/${rootGroup}/children/?filter={query}&recurse=true",
							"delete": "${page.url.context}/proxy/alfresco/api/groups/{item.shortName}",
						},
						"GROUP": {
							"format": "authority",
							"get": "${page.url.context}/proxy/alfresco/api/orgstruct/group/{shortName}/children/",
							"add": "${page.url.context}/proxy/alfresco/api/groups/{parent.shortName}/children/{item.fullName}",
							"delete": "${page.url.context}/proxy/alfresco/api/groups/{parent.shortName}/children/{item.fullName}",
						}
					},
					titles: {
						"root": "{title}",
						"GROUP": "{displayName} ({shortName})",
						"USER": "{firstName} {lastName} ({shortName})",
					},
					errors: [
						{
							"regexp": "regexp.add-item-failure-cyclic",
							"message": "message.add-item-failure-cyclic"
						}
					],
				},
				forms: {
					destination: {
						"root": "GROUP__orgstruct_home_",
						"GROUP": "{fullName}",
					},
					errors: [
						{
							"regexp": "regexp.create-group-failed-exists",
							"message": "message.create-group-failed-exists"
						}
					],
					nodeId: {
						"": "{nodeRef}"
					},
				},
				toolbar: { 
					buttons: {
						"root": [ "search", "createBranch" ],
						"search": [ "search", "resetSearch" ],
						"GROUP-branch": [ "createBranch", "createRole", "addGroup" ],
						"GROUP-role": [ "addUser" ],
						"": [],
					},
				},
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
						"GROUP-group": [ "convertToBranch", "convertToRole", "editItem", "deleteItem" ],
						"GROUP-branch": [ "convertToGroup", "editItem", "deleteItem" ],
						"GROUP-role": [ "convertToGroup", "editItem", "deleteItem" ],
						"USER": [ "editItem", "deleteItem" ]
					},
				},
				list: {
					sorting: {
						"": [
							{ by: "{authorityType}" }, // GROUP, USER
							{ by: "{groupType}", descend: true }, // role, branch, <none>
							{ by: "{roleIsManager}", descend: true }, // managers first
							{ by: "{firstName}-{lastName}-{displayName}" } // firstName/lastName for users, displayName for groups
						],
					},
					buttons: {
						"GROUP-group": [ "convertToBranch", "convertToRole", "editItem", "deleteItem" ],
						"GROUP-branch": [ "convertToGroup", "editItem", "deleteItem" ],
						"GROUP-role": [ "convertToGroup", "editItem", "deleteItem" ],
						"USER": [ "editItem", "deleteItem" ]
					},
				},
			},
			{
				name: "orgmeta",
				model: {
					formats: {
						"groupType": {
							name: "groupType-{name}",
							keys: [ "groupType-{name}", "groupType" ]
						},
						"branchType": {
							name: "branchType-{name}",
							keys: [ "groupSubType", "branchType" ]
						},
						"roleType": {
							name: "roleType-{name}",
							keys: [ "groupSubType", "roleType" ],
						},
					},
					item: {
						"branchType": {
							"format": "branchType",
							"get": "${page.url.context}/proxy/alfresco/api/orgmeta/branch/{name}",
						},
						"roleType": {
							"format": "roleType",
							"get": "${page.url.context}/proxy/alfresco/api/orgmeta/role/{name}",
						},
					},
					children: {
						"root": {
							"format": "groupType",
							"get": "${page.url.context}/proxy/alfresco/api/orgmeta/",
						},
						"groupType-branch": {
							"format": "branchType",
							"get": "${page.url.context}/proxy/alfresco/api/orgmeta/branch",
							"delete": "${page.url.context}/proxy/alfresco/api/orgmeta/branch/{item.name}",
						},
						"groupType-role": {
							"format": "roleType",
							"get": "${page.url.context}/proxy/alfresco/api/orgmeta/role",
							"delete": "${page.url.context}/proxy/alfresco/api/orgmeta/role/{item.name}",
						},
					},
					titles: {
						"groupType": "{name}",
						"groupType-branch": "${msg("item.branch-type.label")}",
						"groupType-role": "${msg("item.role-type.label")}",
						"groupSubType": "{title} ({name})",
					},
					errors: [
						{
							"regexp": "regexp.delete-group-type-failed-referenced",
							"message": "message.delete-group-type-failed-referenced"
						}
					],
				},
				forms: {
					destination: {
						"groupType": "{root}",
					},
					errors: [
						{
							"regexp": "regexp.create-group-type-failed-exists",
							"message": "message.create-group-type-failed-exists"
						}
					],
					nodeId: {
						"": "{nodeRef}"
					},
				},
				tree: {
					buttons: {
						"groupType-branch": [ "createBranchType" ],
						"groupType-role": [ "createRoleType" ],
						"groupSubType": [ "editItem", "deleteItem" ],
					},
				},
				list: {
					buttons: {
						"groupType-branch": [ "createBranchType" ],
						"groupType-role": [ "createRoleType" ],
						"groupSubType": [ "editItem", "deleteItem" ],
					},
				},
				toolbar: {
					buttons: {
						"groupType-branch": [ "createBranchType" ],
						"groupType-role": [ "createRoleType" ],
					}
				},
			},
			{
				name: "deputies",
				model: {
					formats: {
						"authority": {
							name: "authority-{fullName}",
							keys: [ "{groupType}-manager-{roleIsManager}", "{authorityType}-{groupType}", "{authorityType}", "authority" ]
						},
						"member": {
							"name": "deputy-{deputy}-{userName}",
							keys: [ "available-{available}", "deputy-{deputy}", "manage-{manage}", "member" ],
							calc: function(item) {
								if(typeof item.deputy == "undefined") item.deputy = true;
							}
						},
						"deputy": {
							name: "deputy-true-{userName}",
							keys: [ "available-{available}", "deputy-true", "manage-false", "member" ]
						},
					},
					item: {
						"authority": {
							"format": "authority",
							"get": "${page.url.context}/proxy/alfresco/api/orgstruct/authority/{fullName}",
						},
						"deputy-false": {
							"format": "member",
							"get": "${page.url.context}/proxy/alfresco/api/deputy/{userName}",
						},
						"deputy-true": {
							"format": "deputy",
							"get": "${page.url.context}/proxy/alfresco/api/deputy/{userName}",
						},
					},
					children: {
						"search": {
							"format": "authority",
							"get": "${page.url.context}/proxy/alfresco/api/orgstruct/group/${rootGroup}/children/?filter={query}&recurse=true&role=true&user=true&default=false",
						},
						"GROUP": {
							"format": "member",
							"get": "${page.url.context}/proxy/alfresco/api/deputy/{fullName}/members",
							"add": "${page.url.context}/proxy/alfresco/api/deputy/{parent.fullName}/deputies?users={item.userName}",
							"delete": "${page.url.context}/proxy/alfresco/api/deputy/{parent.fullName}/deputies?users={item.userName}",
						},
						"USER": {
							"format": "deputy",
							"get": "${page.url.context}/proxy/alfresco/api/deputy/{fullName}/deputies",
							"add": "${page.url.context}/proxy/alfresco/api/deputy/{parent.fullName}/deputies?users={item.userName}",
							"delete": "${page.url.context}/proxy/alfresco/api/deputy/{parent.fullName}/deputies?users={item.userName}",
						}
					},
					titles: {
						"root": "{title}",
						"GROUP": "{displayName} ({shortName})",
						"USER": "{firstName} {lastName} ({fullName})",
						"member": "{firstName} {lastName} ({userName})",
					},
				},
				forms: {
					nodeId: {
						"": "{nodeRef}"
					},
				},
				toolbar: { 
					buttons: {
						"root": [ "search" ],
						"search": [ "search", "resetSearch" ],
						"authority": [ "addDeputy" ],
						"": [],
					},
				},
				tree: {
					buttons: {
						"deputy-true": [ "editItem", "deleteItem" ],
						"": [ "editItem" ]
					},
				},
				list: {
					buttons: {
						"deputy-true": [ "editItem", "deleteItem" ],
						"": [ "editItem" ]
					},
				},
			}
		],
	}).setMessages(${messages});
//]]></script>

<@orgstructlib.renderOrgstructBody el />