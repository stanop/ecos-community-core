<@markup id="css" >
    <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/citeck/components/document-subscribers/document-subscribers.css" group="document-subscribers" />
</@>

<@markup id="js">
    <@script type="text/javascript" src="${page.url.context}/res/citeck/components/document-subscribers/document-subscribers.js" group="document-subscribers" />
</@>

<#assign id=args.htmlid?js_string>

<div id="${id}" class="document-subscribers document-details-panel">

    <div id="${id}-body" class="panel-body">
        <div id="${id}-subsrcibers-button" class="subsrcibers-link">
		</div>
    </div>
</div>
<@markup id="js">
	<#-- JavaScript Dependencies -->
	<@script type="text/javascript" src="${url.context}/res/citeck/components/document-subscribers/document-subscribers.js" group="document-details" />
</@>

<script type="text/javascript">//<![CDATA[
YAHOO.util.Event.onContentReady("${id}", function() {

	var component = new Citeck.widget.DocumentSubscribers("${id}");
	component.setOptions({
		nodeRef: "${nodeRef?js_string}",
		assocType: "${assocType!''}",
		currentUserId:"${currentUserId}"
	}).setMessages(${messages});
});
//]]></script>