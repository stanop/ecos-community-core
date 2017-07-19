<#import "view-attributes.lib.ftl" as wa />
<#escape x as jsonUtils.encodeJSONString(x)>
{
	attributes: [ <@wa.renderElement view /> ]
}
</#escape>


