<#import "/ru/citeck/views/views.lib.ftl" as views />

<#assign el = args.htmlid?html />

<#if view??>
    <@standalone>
        <@markup id="css" >
            <@views.nodeViewStyles />
        </@>

        <@markup id="js">
            <@views.nodeViewScripts />
        </@>

        <@markup id="widgets">
            <@views.nodeViewWidget nodeRef=userRef />
        </@>

        <@markup id="html">
            <div id="${el}-body" class="user-profile node-view static" <#if mode == "view">style="padding: 20px 40px;"</#if>>
                <div id="profile-view">
                    <@views.renderViewContainer view args.htmlid />
                    <#if mode == "view" && writeMode>
                        <a class="user-profile__edit-link" href="${page.url.getUrl() + "?mode=edit"}">${msg("profile.edit")}</a>
                    </#if>
                </div>
            </div>
        </@>
    </@>
</#if>