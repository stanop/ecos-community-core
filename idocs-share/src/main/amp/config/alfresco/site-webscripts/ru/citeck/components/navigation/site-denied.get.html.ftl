<#if !(site.allowed!false)>
<script>
YAHOO.util.Event.onAvailable("bd", function() {
	var Dom = YAHOO.util.Dom, 
		bd = Dom.get("bd");
	Dom.setStyle(bd, "display", "none");
	bd.outerHTML += '<div class="status-banner theme-bg-color-2 theme-border-4" style="padding:10px;margin:10px;">${msg("message.site-denied", site.id)}</div>'
});
</script>
</#if>