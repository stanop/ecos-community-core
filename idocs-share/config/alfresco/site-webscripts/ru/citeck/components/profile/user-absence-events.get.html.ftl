<@markup id="css" >
<#-- CSS Dependencies -->
    <@link href="${url.context}/res/components/profile/profile.css" group="profile"/>
</@>

<@markup id="js">
<#-- JavaScript Dependencies -->
</@>

<@markup id="widgets">
    <@createWidgets group="profile"/>
</@>

<@markup id="html">
    <@uniqueIdDiv>
        <#assign el=args.htmlid?html>
    <div id="${el}-body" class="profile">
    </div>
    </@>
</@>

