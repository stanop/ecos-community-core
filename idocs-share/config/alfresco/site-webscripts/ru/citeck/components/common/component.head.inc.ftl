<#macro dependenciesFromConfig configName >
	<#if configName?? >
		<#if config??>
			<!-- config present -->
			<#if config.scoped[configName]??>
				<#assign customConfig = config.scoped[configName] />
				<#if customConfig.dependencies??>
					<!-- dependencies present -->
					<#assign dependencies = customConfig.dependencies />
					<#if dependencies.childrenMap??>
						<!-- childrenMap present -->
						<#if dependencies.childrenMap.js??>
							<!-- js present -->
							<#list dependencies.childrenMap.js as item>
<@script type="text/javascript" src="${url.context}/res/${item.attributes.src}"></@script>
							</#list>
						</#if>
						<#if dependencies.childrenMap.css??>
							<!-- css present -->
							<#list dependencies.childrenMap.css as item>
<@link rel="stylesheet" type="text/css" href="${url.context}/res/${item.attributes.src}" />
							</#list>
						</#if>
						<#if dependencies.childrenMap.messages??>
							<!-- messages present -->
							<#list dependencies.childrenMap.messages as messagesNode>
								<#if messagesNode.attributes["scope"]??>
									<#assign scopeArg = "&scope=" + messagesNode.attributes["scope"] />
								<#else>
									<#assign scopeArg = "" />
								</#if>
								<#if messagesNode.attributes["component"]??>
									<#assign componentId=messagesNode.attributes["component"]>
<script src="${url.context}/service/components/${componentId}/messages?format=js${scopeArg}"></script>
								</#if>
								<#if messagesNode.attributes["resource"]??>
									<#assign resource=messagesNode.attributes["resource"]>
<script src="${url.context}/service/components/messages?resource_bundle=${resource}&format=js${scopeArg}"></script>
								</#if>
							</#list>
						</#if>
					</#if>
				</#if>
			</#if>
		</#if>
	</#if>
</#macro>
