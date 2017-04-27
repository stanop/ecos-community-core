<#macro renderTestResult result>
<#assign success = result.wasSuccessful() />
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"success": ${success?string},
	"runCount": ${result.runCount?c},
	"runTime": ${result.runTime?c},
	"ignoreCount": ${result.ignoreCount?c},
	"failureCount": ${result.failureCount?c},
	"failures": [
	<#list result.failures as failure>
		{
			"message": "${failure.message!}",
			"testHeader": "${failure.testHeader!}",
			"trace": "${failure.trace!}"
		}<#if failure_has_next>,</#if>
	</#list>
	]
}
</#escape>
</#macro>
<@renderTestResult result />