<#assign id=args.htmlid?js_string>

<script type="text/javascript">
    new Citeck.widget.CaseStatus("${id}").setOptions({
        nodeRef: "${nodeRef}",
        isPendingUpdate: ${isPendingUpdate?string},
        htmlid: "${id}"
    }).setMessages(${messages});
</script>

<style type="text/css">
    #${id}-statusName.loading {
        background: url(${url.context}/res/citeck/components/invariants/images/loading.gif) no-repeat center;
        width: 40px;
    }
    #${id} .status-line-el {
        display:inline-block
    }
</style>

<div id="${id}" class="case-status document-details-panel">
    <h2 id="${id}-heading" class="alfresco-twister">
        <div id="${id}-header" class="status-line-el">${msg("header.status")}</div>:
        <div id="${id}-statusName" class="panel-body statusName loading status-line-el"></div>
    </h2>
</div>
