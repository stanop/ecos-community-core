<#import "/ru/citeck/components/orgstruct/orgstruct.lib.ftl" as orgstruct />

<#assign el=args.htmlid?html />
<script type="text/javascript">//<![CDATA[
var orgstruct = new Alfresco.component.ConsoleOrgstruct("${el}").setOptions({
    currentFilter: "delegates",
    filters: [
        {
            name: "delegates",
            model: {
                formats: {
                    "authority": {
                        name: "authority-{fullName}",
                        keys: [ "{groupType}-manager-{roleIsManager}", "{authorityType}-{groupType}", "{authorityType}", "delegate-{delegate}", "manage-{manage}", "authority", "available-{available}"]
                    },
                    "member": {
                        "name": "delegate-{delegate}-{userName}",
                        keys: [ "available-{available}", "delegate-{delegate}", "manage-{manage}", "member" ],
                        calc: function(item) {
                            if(typeof item.delegate == "undefined") item.delegate = true;
                        }
                    },
                    "delegate": {
                        name: "delegate-true-{userName}",
                        keys: [ "available-{available}", "delegate-true", "manage-false", "member", "canDelete-{canDelete}" ]
                    },
                },
                item: {
                    "authority": {
                        "format": "authority",
                        "get": "${page.url.context}/proxy/alfresco/api/orgstruct/authority/{fullName}",
                    },
                    "delegate-false": {
                        "format": "member",
                        "get": "${page.url.context}/proxy/alfresco/api/delegate/{userName}",
                    },
                    "delegate-true": {
                        "format": "delegate",
                        "get": "${page.url.context}/proxy/alfresco/api/delegate/{userName}",
                    },
                },
                children: {
                    "root": {
                        "format": "authority",
                        "get": "${page.url.context}/proxy/alfresco/api/delegate/currentUserRoles",
                    },
                    "GROUP": {
                        "format": "member",
                        "get": "${page.url.context}/proxy/alfresco/api/delegate/{fullName}/members",
                        "add": "${page.url.context}/proxy/alfresco/api/delegate/{parent.fullName}/delegates?users={item.userName}",
                        "delete": "${page.url.context}/proxy/alfresco/api/delegate/{parent.fullName}/delegates?users={item.userName}",
                    },
                    "USER": {
                        "format": "delegate",
                        "get": "${page.url.context}/proxy/alfresco/api/delegate/{fullName}/delegates",
                        "add": "${page.url.context}/proxy/alfresco/api/delegate/{parent.fullName}/delegates?users={item.userName}",
                        "delete": "${page.url.context}/proxy/alfresco/api/delegate/{parent.fullName}/delegates?users={item.userName}",
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
                    "manage-true": [ "addDelegate" ],
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