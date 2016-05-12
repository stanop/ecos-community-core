<#escape x as jsonUtils.encodeJSONString(x)>
{
	"source": "${sourceNode.nodeRef}",
	"destination": "${destination.nodeRef}",
	"copy": "${copiedNode.nodeRef}"
}
</#escape>