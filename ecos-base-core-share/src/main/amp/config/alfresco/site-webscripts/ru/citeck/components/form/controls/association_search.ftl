<#assign controlId = fieldHtmlId + "-cntrl">
<#assign compactMode = field.control.params.compactMode!false>
<#assign flatButtonMode = field.control.params.flatButtonMode!"false">
<#assign flatButtonMode = flatButtonMode == "true">
<#assign panelHeight = field.control.params.panelHeight!''>
<#assign elementsLocalization = field.control.params.elementsLocalization!'select-search-assoc.picker-button.label'>

<#if field.control.params.precedenceFieldName??>
    <#assign precedenceFieldName = field.control.params.precedenceFieldName />
    <#assign currentValue = form.data[precedenceFieldName]!field.value!"" />
<#else/>
    <#assign precedenceFieldName = "-" />
    <#assign currentValue = field.value!"" />
</#if>    


<script type="text/javascript">//<![CDATA[
(function () {
    <@renderPickerJS field "picker" />

    var resolveDestFolder='';
<#if form.mode != "view">

    var evaluateDLDestFolder = ${field.control.params.evaluateDLDestFolder!"false"};
    if(evaluateDLDestFolder)
    {
        var url = Alfresco.constants.PROXY_URI + "citeck/get-dl-nodeRef?dltype=${field.endpointType}";
        var request = new XMLHttpRequest();
        request.open('GET', url, false);  // `false` makes the request synchronous
        request.send(null);
        if (request.status === 200) {
            if (request.responseText)
            {
                resolveDestFolder = request.responseText;
            }
        }
    }
</#if>

    picker.setOptions({
        <#if page?? && page.url.templateArgs.site??>
            siteId: '${page.url.templateArgs.site!""}',
            pageMode: true,
			searchWholeRepo: ${field.control.params.searchWholeRepo!"false"},
        <#else>
            siteId: "",
            pageMode: false,
			searchWholeRepo: ${field.control.params.searchWholeRepo!"true"},
        </#if>
        <#if field.control.params.showTargetLink??>
            showLinkToTarget: ${field.control.params.showTargetLink},
            <#if page?? && page.url.templateArgs.site??>
                targetLinkTemplate: "${url.context}/page/site/${page.url.templateArgs.site!""}/document-details?nodeRef={nodeRef}",
            <#else>
                targetLinkTemplate: "${url.context}/page/document-details?nodeRef={nodeRef}",
            </#if>
        </#if>
        <#if field.control.params.allowNavigationToContentChildren??>
            allowNavigationToContentChildren: ${field.control.params.allowNavigationToContentChildren},
        </#if>
        <#if field.control.params.itemType?? || field.endpointType??>
            itemType: "${field.control.params.itemType!field.endpointType}",
        </#if>
            multipleSelectMode: ${field.endpointMany?string},
            parentNodeRef: "alfresco://company/home",
        <#if field.control.params.rootNode??>
            rootNode: "${field.control.params.rootNode}",
        </#if>
        <#if field.control.params.onlyFiltered??>
            onlyFiltered: ${field.control.params.onlyFiltered},
        </#if>
		<#if field.control.params.searchFormId??>
			searchFormId: "${field.control.params.searchFormId}",
		</#if>
		<#if field.control.params.createFormId??>
			createFormId: "${field.control.params.createFormId}",
		</#if>
		<#if field.control.params.journalFolderId??>
			journalFolderId: "${field.control.params.journalFolderId}",
		</#if>
		<#if field.control.params.destFolder??>
			destFolder: "${field.control.params.destFolder}",
        <#else>
            <#if field.control.params.evaluateDLDestFolder?? && field.control.params.evaluateDLDestFolder=="true">
                destFolder: resolveDestFolder,
            </#if>
		</#if>
        itemFamily: "node",
        displayMode: "${field.control.params.displayMode!"items"}",
        elementsLocalization: "${elementsLocalization}",
    });
    <#if form.mode == "create">
        picker.selectItems("${field.value}");
    </#if>
    })();

//]]></script>


<div class="form-field <#if flatButtonMode>flat-button</#if>">
<#if form.mode == "view">
    <div id="${controlId}" class="viewmode-field">
        <#if (field.endpointMandatory!false || field.mandatory!false) && currentValue == "">
        <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png"
                                              title="${msg("form.field.incomplete")}"/><span>
        </#if>
        <span class="viewmode-label">${field.label?html}:</span>
        <span id="${controlId}-currentValueDisplay" class="viewmode-value current-values"></span>
    </div>
<#else>
    <#if field.disabled == false && form.mode == "create" && page?? && ((page.url.args[field.name]!"")?length>0) >
        <input type="hidden" id="${controlId}-added" name="${field.name}_added" value="${page.url.args[field.name]}"/>
    <#else>
        <label for="${controlId}">${field.label?html}:<#if field.endpointMandatory!false || field.mandatory!false><span
                class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if><@formLib.renderFieldHelp field=field /></label>

        <div id="${controlId}" class="object-finder">

            <div id="${controlId}-currentValueDisplay" class="current-values"></div>

            <#if field.disabled == false>
                <div id="${controlId}-itemGroupActions" class="show-picker"></div>
                
                <input type="hidden" id="${fieldHtmlId}" name="${precedenceFieldName}" value="${currentValue?html}"/>
                <input type="hidden" id="${controlId}-added" name="${field.name}_added" <#if form.mode=="create">value="${field.value?html}"</#if> />
                <input type="hidden" id="${controlId}-removed" name="${field.name}_removed"/>

                <@renderPickerHTML controlId/>

            </#if>
        </div>
    </#if>
</#if>
</div>

<#macro renderPickerHTML controlId>
    <#assign pickerId = controlId + "-picker">
<div id="${pickerId}" class="picker yui-panel">
    <div id="${pickerId}-head" class="hd">${msg("form.control.object-picker.header")}</div>

    <div id="${pickerId}-body" class="bd">
        <div class="picker-header">
            <div id="${pickerId}-folderUpContainer" class="folder-up" style="display:none;">
                <button id="${pickerId}-folderUp"></button>
            </div>
            <div id="${pickerId}-navigatorContainer" class="navigator" style="display:none;">
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
                <input type="text" class="search-input" name="-" id="${pickerId}-searchText" value="" maxlength="256"/>
                <span class="search-button"><button
                        id="${pickerId}-searchButton">${msg("form.control.object-picker.search")}</button></span>
            </div>
            <div id="${controlId}-mode-selector">
            </div>
        </div>
		<div id="${pickerId}-picker-mode" class="mode picker-mode">
			<div class="yui-g">
                <div id="${pickerId}-left" class="yui-u first panel-left">
                    <div id="${pickerId}-results" class="picker-items" <#if panelHeight!=''>style="height: ${panelHeight}"</#if>>
                        <#nested>
                    </div>
                </div>
                <div id="${pickerId}-right" class="yui-u panel-right">
                    <div id="${pickerId}-selectedItems" class="picker-items" <#if panelHeight!=''>style="height: ${panelHeight}"</#if>></div>
                </div>
            </div>
			<div class="bdft">
				<button id="${controlId}-ok" tabindex="0">${msg("button.ok")}</button>
				<button id="${controlId}-cancel" tabindex="0">${msg("button.cancel")}</button>
			</div>
		</div>
		<div id="${pickerId}-filter-mode" class="mode filter-mode" style="display: none">
			<div id="${pickerId}-filter-dialog-forms" class="forms-container form-fields">
				<div id="${pickerId}-filter-dialog-form" class="share-form"></div>
			</div>
		</div>
		<div id="${pickerId}-create-mode" class="mode create-mode" style="display: none">
			<div id="${pickerId}-create-dialog-forms" class="forms-container form-fields">
				<div id="${pickerId}-create-dialog-form" class="share-form"></div>
			</div>
		</div>
    </div>

</div>

<#--
<div id="${controlId}-filter-dialog" class="orgstruct-picker">
    <div id="${controlId}-filter-dialog-hd" class="hd">
        ${msg("seletct-search-assoc.filter-dialog.title")} &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    </div>
    <div id="${controlId}-filter-dialog-bd" class="bd">
        <div id="${controlId}-filter-dialog-forms" class="forms-container form-fields">
            <div id="${controlId}-filter-dialog-form" class="share-form"></div>
        </div>
    </div>
</div>
-->

</#macro>

<#macro renderPickerJS field picker="picker">
    <#if field.control.params.selectedValueContextProperty??>
        <#if context.properties[field.control.params.selectedValueContextProperty]??>
            <#local renderPickerJSSelectedValue = context.properties[field.control.params.selectedValueContextProperty]>
        <#elseif args[field.control.params.selectedValueContextProperty]??>
            <#local renderPickerJSSelectedValue = args[field.control.params.selectedValueContextProperty]>
        <#elseif context.properties[field.control.params.selectedValueContextProperty]??>
            <#local renderPickerJSSelectedValue = context.properties[field.control.params.selectedValueContextProperty]>
        </#if>
    </#if>

    function getFilter() {
        var contractorType = '${field.control.params.contractorType!""}';
        return  'name!_!!_!contractorType!_!' + contractorType + '!_!';
    }

    Alfresco.ContractorFinder = function(htmlId, currentValueHtmlId) {
        Alfresco.ContractorFinder.superclass.constructor.call(this, htmlId, currentValueHtmlId);
        this.options.objectRenderer = new Alfresco.ContractorRenderer(this);
        return this;
    };

    YAHOO.lang.extend(Alfresco.ContractorFinder, Alfresco.CiteckObjectFinder);
        Alfresco.ContractorRenderer = function(objectFinder) {
        Alfresco.ContractorRenderer.superclass.constructor.call(this, objectFinder);
    };

    Alfresco.ContractorFinder.prototype.onReady = function () {
        Alfresco.ContractorFinder.superclass.onReady.call(this);
    };

    YAHOO.lang.extend(Alfresco.ContractorRenderer, Alfresco.CiteckObjectRenderer);
        Alfresco.ContractorRenderer.prototype.onRefreshItemList = function (layer, args) {
        if (Alfresco.util.hasEventInterest(this, args)) {
            this._updateItems(this.options.parentNodeRef, getFilter())
        }
    };

    var ${picker} = new Alfresco.ContractorFinder("${controlId}", "${fieldHtmlId}").setOptions({
        <#if form.mode == "view" || (field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true"))>disabled: true,</#if>
            field: "${field.name}",
            compactMode: ${compactMode?string},
        <#if field.mandatory??>
            mandatory: ${field.mandatory?string},
        <#elseif field.endpointMandatory??>
            mandatory: ${field.endpointMandatory?string},
        </#if>
        <#if field.control.params.startLocation??>
            startLocation: "${field.control.params.startLocation}",
            <#if form.mode == "edit" && args.itemId??>currentItem: "${args.itemId?js_string}",</#if>
            <#if form.mode == "create" && form.destination?? && form.destination?length &gt; 0>currentItem: "${form.destination?js_string}",</#if>
        </#if>
        <#if field.control.params.startLocationParams??>
            startLocationParams: "${field.control.params.startLocationParams?js_string}",
        </#if>
        currentValue: "<#if form.mode!="create">${currentValue}</#if>",
        <#if field.control.params.valueType??>valueType: "${field.control.params.valueType}",</#if>
        <#if renderPickerJSSelectedValue??>selectedValue: "${renderPickerJSSelectedValue}",</#if>
        <#if field.control.params.selectActionLabelId??>selectActionLabelId: "${field.control.params.selectActionLabelId}",</#if>
        selectActionLabel: "${field.control.params.selectActionLabel!msg("button.select")}",
        minSearchTermLength: ${field.control.params.minSearchTermLength!'1'},
        maxSearchResults: ${field.control.params.maxSearchResults!'50'},
		defaultMode: "${field.control.params.defaultMode!'picker'}"
    }).setMessages(
        ${messages}
    );
</#macro>