<#import "/ru/citeck/components/orgstruct/orgstruct.lib.ftl" as orgstruct />

<@markup id="css" >
    <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/people-finder/people-finder.css" group="profile" />
    <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/people-finder/group-finder.css" group="profile" />
    <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/citeck/components/orgstruct/console.css" group="profile" />
    <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/citeck/components/deputy/deputy.css" group="profile" />
</@>

<@markup id="js">
    <@script type="text/javascript" src="${page.url.context}/res/components/console/consoletool.js" group="profile" />
    <@script type="text/javascript" src="${page.url.context}/res/yui/resize/resize.js" group="profile" />
    <@script type="text/javascript" src="${page.url.context}/res/components/people-finder/people-finder.js" group="profile" />
    <@script type="text/javascript" src="${page.url.context}/res/components/people-finder/group-finder.js" group="profile" />
    <@script type="text/javascript" src="${page.url.context}/res/modules/simple-dialog.js" group="profile" />
    <@script type="text/javascript" src="${page.url.context}/res/citeck/components/dynamic-tree/dynamic-toolbar.js" group="profile" />
    <@script type="text/javascript" src="${page.url.context}/res/citeck/components/orgstruct/picker-dialogs.js" group="profile" />
    <@script type="text/javascript" src="${page.url.context}/res/citeck/components/orgstruct/form-dialogs.js" group="profile" />
    <@script type="text/javascript" src="${page.url.context}/res/citeck/components/orgstruct/console.js" group="profile" />
</@>

<#assign el=args.htmlid?html />
<script type="text/javascript">//<![CDATA[
var orgstruct = new Alfresco.component.ConsoleOrgstruct("${el}").setOptions({
    currentFilter: "deputies",
    filters: [
        {
            name: "deputies",
            model: {
                formats: {
                    "authority": {
                        name: "authority-{fullName}",
                        keys: [ "{groupType}-manager-{roleIsManager}", "{authorityType}-{groupType}", "{authorityType}", "deputy-{deputy}", "manage-{manage}", "authority", "available-{available}"]
                    },
                    "member": {
                        "name": "deputy-{deputy}-isAssistant-{isAssistant}-{userName}",
                        keys: ["available-{available}", "deputy-{deputy}", "manage-{manage}", "member", "deputy-{deputy}-isAssistant-{isAssistant}", "canDelete-{canDelete}"],
                        calc: function(item) {
                            if(typeof item.deputy == "undefined") item.deputy = true;
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
                    "root": {
                        "format": "authority",
                        "get": "${page.url.context}/proxy/alfresco/api/deputy/currentUserRoles",
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
                    "member": "{firstName} {lastName} ({userName})"
                },
            },
            forms: {
                nodeId: {
                    "": "{nodeRef}"
                },
            },
            toolbar: {
                buttons: {
                    "manage-true": ["addDeputy", "addAssistant"],
                    "": [],
                },
            },
            tree: {
                buttons: {
                },
            },
            list: {
                buttons: {
                    "canDelete-true": [ "deleteItem" ]
                },
            },
        }
    ],
}).setMessages(${messages});
//]]></script>

<@orgstruct.renderOrgstructBody el/>