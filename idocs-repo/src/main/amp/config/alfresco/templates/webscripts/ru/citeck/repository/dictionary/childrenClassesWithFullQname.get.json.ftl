<#import "classdetails.lib.ftl" as classdetailsDefLib/>
[
<#list classdefs as classdef>
	<@classdetailsDefLib.classDefJSONForSelectInput classdef=classdef key=classdef_index/><#if classdef_has_next>,</#if>
</#list>
]