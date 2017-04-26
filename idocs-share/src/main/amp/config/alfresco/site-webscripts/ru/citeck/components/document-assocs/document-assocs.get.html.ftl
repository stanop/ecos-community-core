<@markup id="css" >
    <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/object-finder/object-finder.css" group="document-assocs" />
    <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/citeck/components/document-assocs/document-assocs.css" group="document-assocs"/>
</@>

<@markup id="js">
    <@script type="text/javascript" src="${page.url.context}/res/citeck/components/dynamic-tree/hierarchy-model.js" group="document-assocs" />
    <@script type="text/javascript" src="${page.url.context}/res/citeck/components/dynamic-tree/has-buttons.js" group="document-assocs" />
    <@script type="text/javascript" src="${page.url.context}/res/citeck/components/dynamic-tree/dynamic-tree.js" group="document-assocs" />
    <@script type="text/javascript" src="${page.url.context}/res/citeck/components/dynamic-tree/dynamic-tree-picker.js" group="document-assocs" />
    <@script type="text/javascript" src="${page.url.context}/res/components/object-finder/object-finder.js" group="document-assocs" />
    <@script type="text/javascript" src="${page.url.context}/res/citeck/components/document-assocs/document-assocs.js" group="document-assocs" />
</@>

<#if nodeRef?? && assocs??>
    <#assign el=args.htmlid?js_string>
    <#assign pickerId = el + "-picker">

<div id="${pickerId}" class="assocs-picker yui-panel yui-module yui-overlay dynamic-tree-picker" style="visibility: hidden; width: 60em;">
    <div id="${pickerId}-head" class="hd" style="cursor: move;">${msg("create-button.label")}</div>
    <div id="${pickerId}-body" class="bd">
        <div class="picker-header">
            <div id="${pickerId}-folderUpContainer" class="folder-up">
                <button id="${pickerId}-folderUp"></button>
            </div>
            <div id="${pickerId}-navigatorContainer" class="navigator">
                <button id="${pickerId}-navigator"></button>
                <div id="${pickerId}-navigatorMenu" class="yuimenu">
                    <div class="bd">
                        <ul id="${pickerId}-navigatorItems" class="navigator-items-list">
                            <li>&nbsp;</li>
                        </ul>
                    </div>
                </div>
            </div>
            <div id="${pickerId}-searchContainer" class="search">
                <input type="text" class="search-input" name="-" id="${pickerId}-searchText" value="" maxlength="256">
                <span class="search-button">
                    <span class="yui-button yui-push-button" id="${pickerId}-searchButton">
                        <span class="first-child">
                            <button type="button" tabindex="0" id="${pickerId}-searchButton-button">${msg("search-button.label")}</button>
                        </span>
                    </span>
                </span>
            </div>
        </div>
        <div class="yui-g">
            <div id="${pickerId}-left" class="yui-u first panel-left yui-resize">
                <div id="${pickerId}-results" class="picker-items loading dynamic-tree-list dynamic-tree color-hover">
                    <div class="ygtvitem" id="ygtv0">
                        <div class="ygtvchildren" id="ygtvc0" style="display:none;"></div>
                    </div>
                </div>
                <div id="yui-gen29" class="yui-resize-handle yui-resize-handle-r"><div class="yui-resize-handle-inner-r"></div></div></div>
            <div id="${pickerId}-right" class="yui-u panel-right">
                <div id="${pickerId}-selectedItems" class="picker-items loading dynamic-tree-list dynamic-list color-hover">
                    <div class="ygtvitem" id="ygtv1"><div class="ygtvchildren" id="ygtvc1" style="display:none;"></div>
                    </div>
                </div>
            </div>
        </div>
        <div class="bdft">
            <span class="yui-button yui-push-button" id="${el}-ok"><span class="first-child"><button type="button" tabindex="0" id="${el}-ok-button">${msg("button.ok")}</button></span></span>
            <span class="yui-button yui-push-button" id="${el}-cancel"><span class="first-child"><button type="button" tabindex="0" id="${el}-cancel-button">${msg("button.cancel")}</button></span></span>
        </div>
        <form name="frm_${pickerId}"></form>
    </div>
</div>

<div id="${el}" class="document-assocs document-details-panel">
    <h2 id="${el}-heading" class="thin dark">
        ${msg("header.assocs")}
        <span id="${el}-heading-actions" class="alfresco-twister-actions">
            <a id="${el}-create-button">&nbsp;</a>
        </span>
    </h2>
    <div id="${el}-body" class="panel-body">
        <div id="${el}-message"></div>
    </div>
</div>

<#macro renderAssocList list>
    [
        <#list list as item>
        {
            "name": "${item.name?js_string}",
            "direction": "${item.direction?js_string}",
            "directed": ${item.directed?string}
        }<#if item_has_next>,</#if>
        </#list>
    ]
</#macro>

<#macro renderCells cells>
    [ <#list cells as cell>"${cell}"<#if cell_has_next>,</#if></#list> ]
</#macro>

<script type="text/javascript">//<![CDATA[
YAHOO.util.Event.onContentReady("${el}", function() {
    var model = {
        formats: {
            //actionGroupId is folder-picker or document-picker
            "item": {
                name: "{nodeRef}",
                keys: [ "selected-{selected}", "item", "{actionGroupId}" ],
                calc: function(item) {
                    item.nodeRefForURL = item.nodeRef.replace("://", "/");
                },
            },
            "site": {
                name: "site-{shortName}",
                keys: [ "site-{visibility}", "site" ]
            },
            "selected-items": {
                name: "selected-items",
                keys: [ "selected-items" ]
            }
        },
        item: {
            "": {
                "format": "item",
                "get": "${url.context}/service/citeck/components/documentlibrary/data/node/{nodeRefForURL}?view=picker",
                "resultsList": "item"
            }
        },
        children: {
            "root": {
                "format": "item",
                "get": "${url.context}/service/citeck/components/documentlibrary/data/doclist/treenode/node/alfresco/company/home?view=picker",
                "resultsList": "items"
            },
            "companyhome": {
                "format": "item",
                "get": "${url.context}/service/citeck/components/documentlibrary/data/doclist/treenode/node/alfresco/company/home?view=picker",
                "resultsList": "items"
            },
            "search": {
                "format": "item",
                "get": "${url.context}/service/citeck/components/documentlibrary/data/doclist/treenode/node/alfresco/company/home?view=picker&filter={query}",
                "resultsList": "items"
            },
            "userhome": {
                "format": "item",
                "get": "${url.context}/service/citeck/components/documentlibrary/data/doclist/treenode/node/alfresco/user/home?view=picker",
                "resultsList": "items"
            },
            "siteshome": {
                "format": "site",
                "get": "${url.context}/proxy/alfresco/api/sites"
            },
            "site": {
                "format": "item",
                "get": "${url.context}/service/citeck/components/documentlibrary/data/doclist/treenode/site/{shortName}/documentLibrary?view=picker",
                "resultsList": "items"
            },
            "folder-picker": {
                "format": "item",
                "get": "${url.context}/service/citeck/components/documentlibrary/data/doclist/treenode/node/{nodeRefForURL}?view=picker",
                "resultsList": "items"
            },
            "selected-items": {
                "format": "item"
            }
        },
        titles: {
            "root": "{title}",
            "sites": "{title}",
            "site": "{title}",
            "item": "{displayName}"
        },
    };


    var picker = new Citeck.widget.DynamicTreePicker("${pickerId}").setOptions({
        model: model,
        tree: {
            buttons: {
                "document-picker": [ "itemSelect" ],
                "selected-yes": [ "itemUnselect" ]
            }
        },
        list: {
            buttons: {
                "selected-yes": [ "itemUnselect" ]
            }
        },
        currentRoot: "siteshome",
        roots: [
            {
                name: "siteshome",
                keys: [ "siteshome" ],
                title: "${msg("folder.siteshome.label")}"
            },
            {
                name: "userhome",
                keys: [ "userhome" ],
                title: "${msg("folder.userhome.label")}"
            }
        ]
    });

    var component = new Citeck.widget.DocumentAssocs("${el}").setOptions({
        nodeRef: "${nodeRef?js_string}",

        <#if columns??>columns: ${columns},</#if>
        
        visible: <@renderAssocList assocs.visible />,
        addable: <@renderAssocList assocs.addable />,
        removeable: <@renderAssocList assocs.removeable />,
        createAssocPicker: picker
    }).setMessages(${messages});

    Alfresco.util.createTwister("${el}-heading", "Citeck.widget.DocumentAssocs", { panel: "${el}-body" });
});
//]]></script>

</#if>
