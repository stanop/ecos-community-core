<@markup id="css" >
    <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/citeck/components/card/cardmodes.css" group="card"/>
</@>

<#assign id = args.htmlid?js_string />
<#if nodeRef?? && modes?size != 0>
	<#escape x as x?html>
	<div id="${id}-tabs" class="header-tabs">
			<span class="header-tab <#if modeId == "">current</#if>">
				<a href="card-details?nodeRef=${nodeRef}"
						title="${msg("card.mode.default.description")}">
					${msg("card.mode.default.title")}
				</a>
			</span>
		<#list modes as mode>
			<span class="header-tab <#if modeId == mode.id>current</#if>">
				<a href="card-details?nodeRef=${nodeRef}&mode=${mode.id}" 
						<#if mode.description??>title="${mode.description}"</#if>>
					<#if mode.title??>${mode.title}<#else>${mode.id}</#if>
				</a>
			</span>
		</#list>
	</div>
	</#escape>
</#if>
