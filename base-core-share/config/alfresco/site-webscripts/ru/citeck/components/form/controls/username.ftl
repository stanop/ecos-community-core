<#--
	control to render fields that contain username
	as a link to user profile page
-->
<script type="text/javascript">//<![CDATA[
Alfresco.util.Ajax.jsonGet({
	url: Alfresco.constants.PROXY_URI + "api/people/${field.value?js_string}",
	successCallback: {
		fn: function(response) {
			var user = response.json;
			var names = [];
			if(user.firstName) names.push(user.firstName);
			if(user.middleName) names.push(user.middleName);
			if(user.lastName) names.push(user.lastName);
			var name = names.join(" ") || user.userName;
			Dom.get("${fieldHtmlId}-text").innerHTML = 
			<#if field.control.params.plainText!"false" == "true">
				name
			<#else/>
				Alfresco.util.userProfileLink(user.userName, name)
			</#if>;
		}
	}
});
//]]></script>

<div class="form-field">
	<span class="viewmode-label">${field.label?html}:</span>
	<span class="viewmode-value" id="${fieldHtmlId}-text">${field.value?html}</span>
	<input id="${fieldHtmlId}" type="hidden" name="${field.name}" value="${field.value?html}" />
</div>