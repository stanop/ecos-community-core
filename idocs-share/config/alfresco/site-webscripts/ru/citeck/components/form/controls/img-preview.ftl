<script type="text/javascript">//<![CDATA[
function loadFail(e) {
    var img = YAHOO.util.Dom.get("${args.htmlid}-img");
    img.className = 'hidden';
    img.parentNode.innerHTML = "${msg("img.preview.fail")}";
    var preview =  YAHOO.util.Dom.get("${args.htmlid}-img-preview");
    preview.className = 'img-preview-fail';
};
//]]></script>

<div id="${args.htmlid}-img-preview">
    <span>
        <img id="${args.htmlid}-img"
             alt="${field.label}"
             src="${url.context}/proxy/alfresco/citeck/print/content?nodeRef=${form.arguments.itemId}"
             onerror="loadFail()"
             style="<#if field.control.params.width??>width: ${field.control.params.width}</#if>"
        />
    </span>
</div>
