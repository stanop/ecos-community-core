<#assign el = args.htmlid />
<div class="footer footer-com modules-info-footer">
    <div class="modules-info">
		<span class="modules-title">${msg("modules.repo")}:</span>
		<span id="${el}-repo"></span>
    </div>
    <div class="modules-info">
		<span class="modules-title">${msg("modules.share")}:</span>
		<span id="${el}-share"></span>
    </div>
</div>
<script type="text/javascript">//<[!CDATA[
new Citeck.component.ModulesInfo("${el}");
//]]></script>