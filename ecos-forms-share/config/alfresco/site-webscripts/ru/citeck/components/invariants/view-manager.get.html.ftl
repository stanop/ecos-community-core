<#assign onsubmit><#if page.url.args.onsubmit??>${page.url.args.onsubmit}<#elseif args.onsubmit??>${args.onsubmit}</#if></#assign>
<#assign oncancel><#if page.url.args.oncancel??>${page.url.args.oncancel}<#elseif args.oncancel??>${args.oncancel}</#if></#assign>
<#assign redirect><#if page.url.args.redirect??>${page.url.args.redirect}<#elseif args.redirect??>${args.redirect}</#if></#assign>

<@markup id="js">
  <@script type="text/javascript" src="${url.context}/res/citeck/components/invariants/view-manager.js" group="node-view"/>
  <@inlineScript group="node-view">
    new Citeck.invariants.NodeViewManager("${args.runtimeKey}").setOptions({
      <#if onsubmit??>onsubmit: "${onsubmit}",</#if>
      <#if oncancel??>oncancel: "${oncancel}",</#if>
      <#if redirect??>redirect: ${redirect}</#if>
    });
  </@inlineScript>
</@>