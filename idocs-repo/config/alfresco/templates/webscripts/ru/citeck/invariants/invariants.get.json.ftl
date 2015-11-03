<#import "invariants.lib.ftl" as inv />

<#escape x as jsonUtils.encodeJSONString(x)>
{
	"model": {
		"person": <#if model.person??>"${model.person.nodeRef}"<#else>null</#if>,
		"companyhome": <#if model.companyhome??>"${model.companyhome.nodeRef}"<#else>null</#if>,
		"userhome": <#if model.userhome??>"${model.userhome.nodeRef}"<#else>null</#if>
	},
	"invariants": [
	<#list invariants as invariant>
		<@inv.renderInvariant invariant /><#if invariant_has_next>,</#if>
	</#list>
	]
}
</#escape>