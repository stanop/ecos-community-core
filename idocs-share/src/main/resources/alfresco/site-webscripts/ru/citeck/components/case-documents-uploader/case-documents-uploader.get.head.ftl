<#include "/org/alfresco/components/component.head.inc">
<#include "/ru/citeck/components/common/component.head.inc.ftl">

<#include "/org/alfresco/components/form/form.dependencies.inc">

<@dependenciesFromConfig "DocumentListDependencies" />
<@dependenciesFromConfig "DocLibCustom" />

<#--  web-preview dependencies  -->
<@script src="${url.context}/res/components/preview/web-preview.js" />
<@script src="${url.context}/res/components/preview/WebPreviewer.js" />
<@script src="${url.context}/res/js/flash/extMouseWheel.js" />
<@script src="${url.context}/res/components/preview/StrobeMediaPlayback.js" />
<@script src="${url.context}/res/components/preview/Video.js" />
<@script src="${url.context}/res/components/preview/Audio.js" />
<@script src="${url.context}/res/components/preview/Flash.js" />
<@script src="${url.context}/res/components/preview/Image.js" />
<@script src="${url.context}/res/components/preview/PdfJs.js" />
<@script src="${url.context}/res/components/preview/pdfjs/compatibility.js" />
<@script src="${url.context}/res/components/preview/pdfjs/pdf.js" />
<@script src="${url.context}/res/components/preview/pdfjs/pdf.worker.js" />
<@script src="${url.context}/res/components/preview/spin.js" />

<@link href="${url.context}/res/components/preview/web-preview.css" />
<@link href="${url.context}/res/components/preview/WebPreviewerHTML.css"  />
<@link href="${url.context}/res/components/preview/StrobeMediaPlayback.css"  />
<@link href="${url.context}/res/components/preview/Audio.css"  />
<@link href="${url.context}/res/components/preview/Image.css"  />
<@link href="${url.context}/res/components/preview/PdfJs.css"  />


<#-- versions dependencies -->
<@script type="text/javascript" src="${url.context}/res/citeck/components/document-versions-minimalistic/document-versions.js" />
<@script type="text/javascript" src="${url.context}/res/modules/document-details/revert-version.js" />
<@script type="text/javascript" src="${url.context}/res/modules/document-details/historic-properties-viewer.js" />

<@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/document-versions-minimalistic/document-versions.css" />
<@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/document-details/revert-version.css" />
<@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/document-details/historic-properties-viewer.css" />

<#-- versions-comparison dependencies -->
<@script type="text/javascript" src="${page.url.context}/res/citeck/components/document-versions-comparison/document-versions-comparison.js"></@script>
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/citeck/components/document-versions-comparison/document-versions-comparison.css" />

<@script type="text/javascript" src="${page.url.context}/res/citeck/components/case-documents-uploader/case-documents-uploader.js"></@script>
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/citeck/components/case-documents-uploader/case-documents-uploader.css" />

