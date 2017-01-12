<#assign id=args.htmlid?js_string>
<#if nodeRefExists??>
    <div id="${id}" class="document-versions-comparison document-details-panel">
        <div id="${id}-body" class="panel-body">
            <div class="vers-menu">
                <label class="vc-label">${msg("label.versRef")}</label>
                <div id="${id}-versRef"></div>
            </div>
            <div class="vers-menu">
                <label class="vc-label">${msg("label.nodeRef")}</label>
                <div id="${id}-nodeRef"></div>
            </div>
            <div id="${id}-compare-button" class="compare-link"></div>
        </div>
    </div>
</#if>

<script type="text/javascript">//<![CDATA[
YAHOO.util.Event.onContentReady("${id}", function() {

    var component = new Citeck.widget.DocumentVersionsComparison("${id}");
    component.setOptions({
        nodeRef: "${nodeRef?js_string}"
    }).setMessages(${messages});
});
//]]></script>