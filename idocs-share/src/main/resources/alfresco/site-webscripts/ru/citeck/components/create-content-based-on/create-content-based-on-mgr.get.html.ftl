<@markup id="css" >
<#-- CSS Dependencies -->
    <@link href="${url.context}/res/components/create-content/create-content-mgr.css" group="create-content"/>
</@>

<@markup id="js">
<#-- JavaScript Dependencies -->
    <@script src="${url.context}/res/components/create-content/create-content-mgr.js" group="create-content"/>
</@>

<@markup id="widgets">
    <@createWidgets group="create-content"/>
</@>

<@markup id="html">
    <@uniqueIdDiv>
    <div class="create-content-mgr">
        <#assign html_id_name="create-content-based-on-title-${args.itemId}" />
        <div class="heading">${msg("create-content-based-on-mgr.heading", '<span id="${html_id_name}"></span>')}</div>
        <script type="text/javascript">
            Alfresco.util.Ajax.request({
                method: Alfresco.util.Ajax.GET,
                url: Alfresco.constants.PROXY_URI + "citeck/node?nodeRef=${args.itemId}&props=cm:name",
                successCallback: {
                    scope: this,
                    fn: function(response) {
                        var name = response.json.props["cm:name"];
                        document.getElementById("${html_id_name}").innerHTML = name;
                    },
                }, failureCallback: {
                    scope: this,
                    fn: function(response) {
                        Alfresco.util.PopupManager.displayMessage({
                            text: this.msg("message.details.failure")
                        });
                    }
                },
                execScripts: true
            });
        </script>
    </div>
    </@>
</@>
