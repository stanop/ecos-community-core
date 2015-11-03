<#import "webscript-messages.lib.ftl" as msg />
(function() {
	if(typeof Alfresco == "undefined") Alfresco = {};
	Alfresco.messages = Alfresco.messages || { global: {}, scope: {} };
	var messages = <@msg.renderMessagesJSON webscriptMessages />;
	<#if scope??>
	var base = Alfresco.messages.scope["${scope?js_string}"] || {};
	Alfresco.messages.scope["${scope?js_string}"] = base;
	<#else>
	var base = Alfresco.messages.global;
	</#if>
	for(var i in messages) {
		if(messages.hasOwnProperty(i)) {
			base[i] = messages[i];
		}
	}
})();