<#import "views.lib.ftl" as views />

<#escape x as jsonUtils.encodeJSONString(x)>{
"view": <@views.renderView view />,
"canBeDraft": ${canBeDraft?string}
}</#escape>

