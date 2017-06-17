<#if formUI == "true">
   <@formLib.renderFormsRuntime formId=formId />
</#if>

<div id="${args.htmlid}-dialog">
   <div class="bd">

      <div id="${formId}-container" class="form-container">

   
         <#if form.showCaption?exists && form.showCaption>
            <div id="${formId}-caption" class="caption"><span class="mandatory-indicator">*</span>${msg("form.required.fields")}</div>
         </#if>
      
         <form id="${formId}" method="${form.method}" accept-charset="utf-8" enctype="${form.enctype}" action="${form.submissionUrl}">
   
         <#if form.destination??>
            <input id="${formId}-destination" name="alf_destination" type="hidden" value="${form.destination}" />
         </#if>
   
            <div id="${formId}-fields" class="form-fields">
               <#list form.structure as item>
                  <#if item.kind == "set">
                     <@renderSetWithColumns set=item />
                  <#else>
                     <@formLib.renderField field=form.fields[item.id] />
                  </#if>
               </#list>
            </div>

            <div class="bdft">
               <input id="${formId}-submit" type="submit" value="${msg("form.button.submit.label")}" />
               &nbsp;<input id="${formId}-cancel" type="button" value="${msg("form.button.cancel.label")}" />
            </div>
      
         </form>

      </div>
   </div>
</div>

<#macro renderSetWithColumns set>
   <div class="set">
   <#if set.appearance??>
      <#if set.appearance == "fieldset">
         <fieldset><legend>${set.label}</legend>
      <#elseif set.appearance == "bordered-panel">
         <div class="set-bordered-panel">
            <div class="set-bordered-panel-heading">${set.label}</div>
            <div class="set-bordered-panel-body">
      <#elseif set.appearance == "panel">
         <div class="set-panel">
            <div class="set-panel-heading">${set.label}</div>
            <div class="set-panel-body">
      <#elseif set.appearance == "title">
         <div class="set-title">${set.label}</div>
      <#elseif set.appearance == "whitespace">
         <div class="set-whitespace"></div>
      </#if>
   </#if>
   
   <#if set.template??>
      <#include "${set.template}" />
   <#else>
      <#list set.children as item>
         <#if item.kind == "set">
            <@renderSetWithColumns set=item />
         <#else>
               <#if (item_index % 2) == 0>
         		 	<div class="yui-g"><div class="yui-u first">
         		<#else>
         			<div class="yui-u">
         		</#if>
         		<@formLib.renderField field=form.fields[item.id] />         	
         		</div>
         	<#if ((item_index % 2) != 0) || !item_has_next></div></#if>
         </#if>
      </#list>
   </#if>
   
   <#if set.appearance??>
      <#if set.appearance == "fieldset">
         </fieldset>
      <#elseif set.appearance == "panel" || set.appearance == "bordered-panel">
            </div>
         </div>
      </#if>
   </#if>
   </div>
</#macro>