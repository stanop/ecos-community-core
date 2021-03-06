<#assign onsubmit><#if page.url.args.onsubmit??>${page.url.args.onsubmit}<#elseif args.onsubmit??>${args.onsubmit}</#if></#assign>
<#assign oncancel><#if page.url.args.oncancel??>${page.url.args.oncancel}<#elseif args.oncancel??>${args.oncancel}</#if></#assign>
<#assign redirect><#if page.url.args.redirect??>${page.url.args.redirect}<#elseif args.redirect??>${args.redirect}</#if></#assign>

<@markup id="js">
  <@inlineScript group="node-view">
      require(['citeck/components/invariants/view-manager'], function() {
          new Citeck.invariants.NodeViewManager("${args.runtimeKey}").setOptions({
            <#if onsubmit?has_content>onsubmit: "${onsubmit}",</#if>
            <#if oncancel?has_content>oncancel: "${oncancel}",</#if>
            <#if redirect?has_content>redirect: ${redirect}</#if>
          });
      });
  </@inlineScript>
</@>