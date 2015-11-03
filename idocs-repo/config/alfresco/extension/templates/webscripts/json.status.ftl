<#escape x as jsonUtils.encodeJSONString(x)>
{
  <#-- Details of the response code -->
  "status" : 
  {
    "code" : ${status.code},
    "name" : "${status.codeName}",
    "description" : "${status.codeDescription}"
  },  
  
  <#-- Exception details -->
  "message" : <@renderMessage status.message />,
  "originalMessage" : "${status.message}",
  "exception" : "<#if status.exception??>${status.exception.class.name}<#if status.exception.message??> - ${status.exception.message}</#if></#if>",
  
  <#-- Exception call stack --> 
  "callstack" : 
  [ 
  	  <#if status.exception??>""<@recursestack exception=status.exception/></#if> 
  ],
  
  <#-- Server details and time stamp -->
  "server" : "${server.edition?xml} v${server.version?xml} schema ${server.schema?xml}",
  "time" : "${date?string('dd.MM.yyyy HH:mm:ss')}"
}
</#escape>

<#macro renderMessage message>
<#escape x as jsonUtils.encodeJSONString(x)>
"${message?replace("^(org.alfresco.scripts.ScriptException|org.activiti.engine.ActivitiException): (Exception while invoking TaskListener: )*[0-9]+ Failed to execute supplied script: [0-9]+ *", "", 'r')?replace(" *[(]AlfrescoJS[#][0-9]+[)]", "", 'r')}"
</#escape>
</#macro>

<#macro recursestack exception>
<#escape x as jsonUtils.encodeJSONString(x)>
   <#if exception.cause??>
      <@recursestack exception=exception.cause/>
   </#if>
   <#if !exception.cause??>
      ,"${exception?string}"
      <#list exception.stackTrace as element>
      ,"${element?string}"
      </#list>
   <#else>
      ,"${exception?string}"
      ,"${exception.stackTrace[0]?string}"
   </#if>
</#escape>
</#macro>