<#assign idhtml=args.htmlid/>
<#assign numsel=field.control.params.numsel/>
<#include "select-member.inc.ftl" />

<div class="form-field">        	
        	
	<@getSelectMemberConteinerHTML conteinerId="${idhtml}" />
	<@getSelectPickerHTML />

</div>

<script type="text/javascript">//<![CDATA[
YAHOO.util.onAvailable("${idhtml}-form", function() {
	<@getSelectPickerJS elemId="${idhtml}-button" number="${numsel}"/>
});
//]]></script>
