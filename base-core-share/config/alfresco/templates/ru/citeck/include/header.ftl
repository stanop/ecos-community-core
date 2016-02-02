<#assign chromelessHeader = (config.scoped['ModuleConfig.global']['chromeless-header'].value)!'false'/>
<#if chromelessHeader == 'true'>
    <@region scope="global" id="share-header" chromeless="true"/>
<#else>
    <@region id="header" scope="global" />
    <@region id="title" scope="template" />
    <@region id="navigation" scope="template" />
    <@region id="task-title" scope="template" />
</#if>
