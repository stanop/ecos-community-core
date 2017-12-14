<#import "orgstruct.lib.ftl" as orgstructlib />

<@markup id="css" >
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/people-finder/people-finder.css" group="orgstruct" />
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/people-finder/group-finder.css" group="orgstruct" />

    <@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/dynamic-tree/dynamic-tree.css" group="orgstruct" />
    
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/orgstruct/orgstruct-icons.css" group="orgstruct" />
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/orgstruct/console.css" group="orgstruct" />
</@>

<@markup id="js">
    <@script type="text/javascript" src="${url.context}/res/components/console/consoletool.js" group="orgstruct" />
    <@script type="text/javascript" src="${url.context}/res/yui/resize/resize.js" group="orgstruct" />
    <@script type="text/javascript" src="${url.context}/res/components/people-finder/people-finder.js" group="orgstruct" />
    <@script type="text/javascript" src="${url.context}/res/components/people-finder/group-finder.js" group="orgstruct" />
    <@script type="text/javascript" src="${url.context}/res/modules/simple-dialog.js" group="orgstruct" />

	<@script type="text/javascript" src="${url.context}/res/components/form/form.js" group="orgstruct" />
	<@script type="text/javascript" src="${url.context}/res/citeck/components/form/select.js" group="orgstruct" />

    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/error-manager.js" group="orgstruct" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/hierarchy-model.js" group="orgstruct" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/has-buttons.js" group="orgstruct" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/dynamic-tree.js" group="orgstruct" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/dynamic-toolbar.js" group="orgstruct" />

    <@script type="text/javascript" src="${url.context}/res/citeck/components/orgstruct/picker-dialogs.js" group="orgstruct" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/orgstruct/form-dialogs.js" group="orgstruct" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/orgstruct/console.js" group="orgstruct" />
</@>

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
                            keys: ["{groupType}-manager-{roleIsManager}", "{authorityType}-{groupType}", "{authorityType}", "deputy-{deputy}", "manage-{manage}", "authority", "available-{available}"]
                        },
                        "member": {
                            "name": "deputy-{deputy}-isAssistant-{isAssistant}-{userName}",
                            keys: ["available-{available}", "deputy-{deputy}", "manage-{manage}", "member", "deputy-{deputy}-isAssistant-{isAssistant}", "canDelete-{canDelete}"],
                            calc: function (item) {
                                if (typeof item.deputy == "undefined") item.deputy = true;
                                if (typeof item.isAssistant == "undefined") item.isAssistant = false;
                            }
                        },
                        "deputy": {
                            name: "deputy-true-isAssistant-{isAssistant}-{userName}",
                            keys: ["available-{available}", "deputy-true", "manage-false", "member", "canDelete-{canDelete}", "isAssistant-{isAssistant}", "deputy-true-isAssistant-{isAssistant}"]
                        },
					},
                    item: {
                        "authority": {
                            "format": "authority",
                            "get": "${page.url.context}/proxy/alfresco/api/orgstruct/authority/{fullName}",
                        },
                        "deputy-false-isAssistant-false": {
                            "format": "member",
                            "get": "${page.url.context}/proxy/alfresco/api/deputy/{userName}",
                        },
                        "deputy-false-isAssistant-true": {
                            "format": "member",
                            "get": "${page.url.context}/proxy/alfresco/api/deputy/{userName}",
                        },
                        "deputy-true-isAssistant-false": {
                            "format": "deputy",
                            "get": "${page.url.context}/proxy/alfresco/api/deputy/{userName}",
                        },
                        "deputy-true-isAssistant-true": {
                            "format": "deputy",
                            "get": "${page.url.context}/proxy/alfresco/api/assistant/{userName}",
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
                            "add": "${page.url.context}/proxy/alfresco/api/deputy/{parent.fullName}/deputies?users={item.userName}&addAssistants={item.isAssistant}",
                            "delete": "${page.url.context}/proxy/alfresco/api/deputy/{parent.fullName}/deputies?users={item.userName}&isAssistants={item.isAssistant}",
                        },
                        "USER": {
                            "format": "deputy",
                            "get": "${page.url.context}/proxy/alfresco/api/deputy/{fullName}/deputies",
                            "add": "${page.url.context}/proxy/alfresco/api/deputy/{parent.fullName}/deputies?users={item.userName}&addAssistants={item.isAssistant}",
                            "delete": "${page.url.context}/proxy/alfresco/api/deputy/{parent.fullName}/deputies?users={item.userName}&isAssistants={item.isAssistant}",
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
                        "authority": ["addDeputy", "addAssistant"],
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