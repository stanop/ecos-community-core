<#import "/ru/citeck/components/orgstruct/orgstruct.lib.ftl" as orgstruct />

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
                        "name": "deputy-{deputy}-{userName}",
                        keys: [ "available-{available}", "deputy-{deputy}", "manage-{manage}", "member" ],
                        calc: function(item) {
                            if(typeof item.deputy == "undefined") item.deputy = true;
                        }
                    },
                    "deputy": {
                        name: "deputy-true-{userName}",
                        keys: [ "available-{available}", "deputy-true", "manage-false", "member", "canDelete-{canDelete}" ]
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
                    "root": {
                        "format": "authority",
                        "get": "${page.url.context}/proxy/alfresco/api/deputy/currentUserRoles",
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
                    "manage-true": [ "addDeputy" ],
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