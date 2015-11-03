<#include "include/alfresco-template.ftl" />

<#-- =================================================== -->
<#-- NOTE: this template is used to force IE10 emulation -->
<#--       on login page                                 -->
<#-- =================================================== -->

<#if !PORTLET>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
   <title><@region id="head-title" scope="global" chromeless="true"/></title>
   <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE10" />
   <#assign PORTLET = true, PORTLET_WAS_FALSE = true />
</#if>
<@templateHeader/>
<#if PORTLET_WAS_FALSE!false>
<#assign PORTLET = false />
</head>
</#if>

<@templateBody type="alfresco-guest">
   <#if outcome??>
      <@region id=outcome scope="page"/>
   <#else>
      <@region id="components" scope="page"/>
   </#if>
</@>

<@templateFooter/>