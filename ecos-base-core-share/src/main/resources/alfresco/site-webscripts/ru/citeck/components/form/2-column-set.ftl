<#if form.mode == "view">
	<#assign twoColumnClass = "yui-g plain" />
<#else>
	<#assign twoColumnClass = "yui-g" />
</#if>
<#list set.children as item>
   <#if item.kind != "set">
      <#if (item_index % 2) == 0>
      <div class="${twoColumnClass}"><div class="yui-u first">
      <#else>
      <div class="yui-u">
      </#if>
      <@formLib.renderField field=form.fields[item.id] />
      </div>
      <#if ((item_index % 2) != 0) || !item_has_next></div></#if>
   </#if>
</#list>
