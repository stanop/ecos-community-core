<#assign webframeworkConfig = config.scoped["WebFramework"]["web-framework"]!>

<@markup id="dojoCacheBust" target="setDojoConfig" action="after" scope="global" >
   <#if webframeworkConfig.dojoEnabled>
     <script type="text/javascript">
        // Enable Cache Bust for testing
        dojoConfig.cacheBust = "${citeckUtils.getModulePackage("ecos-base-core-share").getVersion().toString()}";
     </script>
   </#if>
</@>