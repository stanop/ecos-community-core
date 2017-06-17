<#if config.scoped["${configName}"]?? && config.scoped["${configName}"]["dependencies"]??>
	<#assign jsCfg=config.scoped["${configName}"]["dependencies"].getChildren("js")>
	<#assign cssCfg=config.scoped["${configName}"]["dependencies"].getChildren("css")>
	<#assign messagesCfg=config.scoped["${configName}"]["dependencies"].getChildren("messages")>

	<#assign defaultGroup = args.group!"CustomJsCssMessages" />
	
	<#-- JavaScript Dependencies -->
	<#if jsCfg??><@markup id="js">
		<#list jsCfg as jsNode>
			<#assign src=jsNode.attributes["src"]>
			<#assign group=jsNode.attributes["group"]!defaultGroup />
			<@script src="${url.context}/res${src}" group=group/>
		</#list>
		<#list messagesCfg as messagesNode>
			<#assign group=messagesNode.attributes["group"]!defaultGroup />
			<#if messagesNode.attributes["component"]??>
				<#assign componentId=messagesNode.attributes["component"]>
				<@script src="${url.context}/service/components/${componentId}/messages?format=js" group=group/>
			</#if>
			<#if messagesNode.attributes["resource"]??>
				<#assign resource=messagesNode.attributes["resource"]>
				<@script src="${url.context}/service/components/messages?resource_bundle=${resource}&format=js" group=group/>
			</#if>
		</#list>
	</@></#if>

	<#-- CSS Dependencies -->
	<#if cssCfg??><@markup id="css">
		<#list cssCfg as cssNode>
			<#assign src=cssNode.attributes["src"]>
			<#assign group=cssNode.attributes["group"]!defaultGroup />
			<@link rel="stylesheet" type="text/css" href="${url.context}/res${src}" group=group/>
		</#list>
	</@></#if>

</#if>

<@markup id="widgets">
	<@createWidgets group="CustomJsCssMessages"/>
</@>
