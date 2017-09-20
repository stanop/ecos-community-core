<#assign cdid=args.htmlid?js_string>

<#import "/org/alfresco/components/documentlibrary/include/documentlist.lib.ftl" as doclib />

<div id="${cdid}-upload-dialog" style="visibility: hidden;" class="doc-uploader">
    <div class="hd" style="cursor: move;">${msg("dialog.header.upload")}</div>
    <div class="bd">
        <form id="uploadform" method="POST" action="${url.context}/proxy/alfresco/citeck/cases/case-documents" enctype="multipart/form-data">
            <input id="container" name="container" type="hidden">
            <input id="type" name="type" type="hidden">
            <input id="kind" name="kind" type="hidden">
            <input id="multiple" name="multiple" type="hidden">
            
            <div class="upload-select">
                <select id="${cdid}-container" name="container"></select>
            </div>
            <div class="upload-select" id="${cdid}-type-select-container">
                <select id="${cdid}-type" name="type"></select>
            </div>
            <div class="upload-select" id="${cdid}-kind-select-container">
                <select id="${cdid}-kind" name="kind"></select>
            </div>
            <div class="upload-select">
                <input id="filedata" name="filedata" type="file" style="width: 400px;" disabled="disabled">
            </div>
        </form>
    </div>
</div>`

<div id="${cdid}-view-dialog" style="visibility: hidden">
    <div class="hd" style="cursor: move;">${msg("dialog.header.view")}</div>
    <div class="bd">
        <div id="${cdid}-doclist">
            <#assign args1 = args />
            <#global args = args + { "htmlid": cdid + "-doclist" } />
            <#global sortOptions = [ { "value": "name", "label": "name" } ] />
            <@doclib.documentlistTemplate />
            <#global args = args1 />
        </div>
    </div>
</div>

<div id="${cdid}-intermediate-file-dialog" style="visibility: hidden">
    <div class="hd" style="cursor: move;">${msg("dialog.header.view")}</div>
    <div class="bd" style="overflow: hidden;">
    </div>
</div>

<div class="hidden">
    <div>
        <div class="setDefaultView">&nbsp;</div>
    </div>
</div>

<div id="${cdid}" class="case-documents-uploader document-details-panel">
    <div id="${cdid}-body" class="doc-uploader">
        <div id="${cdid}-case-documents-uploader-header" class="case-documents-uploader-header">
            <div class="case-documents-uploader-buttons">
                <button id="${cdid}-update-button" class="update-button" title="${msg("button.update")}">
                    <i class="fa fa-refresh" aria-hidden="true"></i>
                </button>
            <#if documentUploadPermission?? && documentUploadPermission>
                    <button id="${cdid}-upload-button" class="upload-button" title="${msg("button.upload")}">
                        <i class="fa fa-upload" aria-hidden="true"></i>
                    </button>
            </#if>
            </div>

            <div class="case-documents-uploader-search">
                <input type="text" id="${cdid}-kinds-filter" class="kinds-filter" value="" placeholder="${msg("search.kinds-filter.placeholder")}">
            </div>

            <div class="case-documents-uploader-filters">
                <div class="filter-field">
                    <input id="${cdid}-uploaded-filter" type="checkbox" class="filter-label">
                    <label for="${cdid}-uploaded-filter">${msg("filter.uploaded")}</label>
                </div>
                <div class="filter-field">
                    <input id="${cdid}-mandatory-filter" type="checkbox" class="filter-label">
                    <label for="${cdid}-mandatory-filter">${msg("filter.mandatory")}</label>
        </div>
            </div>
        </div>
        <div id="${cdid}-case-documents-uploader-filter-presets" class="case-documents-uploader-filter-presets">
            <div id="${cdid}-cases-filter-div" class="div-filter">
                <div class="menu-header">
                    ${msg("filter.cases")}
                </div>
                <div id="${cdid}-cases-filter" class="yuimenu">
                    <div class="bd"></div>
                </div>
            </div>
            <div id="${cdid}-types-filter-div" class="div-filter">
                <div class="menu-header">
                    ${msg("filter.types")}
                </div>
                <div id="${cdid}-types-filter" class="yuimenu">
                    <div class="bd"></div>
                </div>
            </div>
        </div>
        <div id="${cdid}-case-documents-uploader-table" class="case-documents-uploader-table">
        </div>
    </div>
</div>

<script type="text/javascript">//<![CDATA[
YAHOO.util.Event.onContentReady("${cdid}", function() {
    var component = new Citeck.widget.CaseDocumentsUploader("${cdid}");
    component.setOptions({
        nodeRef: "${nodeRef?js_string}",
        documentType: "${documentType?js_string}",
        documentKind: "${documentKind?js_string}",

        <#if documentUploadDefaultType??>
            documentUploadDefaultType: "${documentUploadDefaultType}",
        </#if>
        <#if documentUploadDefaultKind??>
            documentUploadDefaultKind: "${documentUploadDefaultKind}",
        </#if>

        // formId for block of properties on intermediate preview dialog window
        intermediateDialog: {
            <#if intermediateDialogFormId??>
                formId: "${intermediateDialogFormId?js_string}"
            </#if>
        }
    }).setMessages(${messages});
});
//]]></script>

<#if args.style??>
    <style type="text/css">
        ${args.style?string}
    </style>
</#if>