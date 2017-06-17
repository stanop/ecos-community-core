<#assign id=args.htmlid?js_string>


<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/document-versions-comparison/document-versions-comparison.css" group="document-versions-comparison" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${url.context}/res/citeck/components/document-versions-comparison/document-versions-comparison.js" group="document-versions-comparison" />
</@>


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

<script type="text/javascript">
    YAHOO.util.Event.onContentReady("${id}", function() {

        new Citeck.widget.DocumentVersionsComparison("${id}").setOptions({
            nodeRef: "${nodeRef?js_string}"
        }).setMessages(${messages});
    });
</script>