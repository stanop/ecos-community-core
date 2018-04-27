<#assign params = viewScope.region.params!{} />
<#assign downloadActionInViewMode = params.downloadActionInViewMode!"false" />

<#if params.columns??>
    <#assign columns = params.columns?replace("\\s+", "", "rm") />
</#if>

<#assign actionIsFirstColumn = params.actionIsFirstColumn!"false" />
<#assign duplicateButton = params.duplicateButton!"false" />

<#-- Parametes:
        * journalType - columns is defaultAttributes ("files-numenclature") [optional]
        * columns     - attributes name on string ("cm:name,tk:type") [optional]
        * maxheight   - maximum height of view-table-container ("150px", "50%") [optional]
        * style       - individual styles. added in the end of control (".style { font-size: 13px; }") [optional]
        
        * highlightedColumnMarker    - marked rows as 'highlighted' if specified attribute exists and 'true' ("cm:content") [optional]
        * highlightedAdditionalClass - additional classes for highlighted rows. use only with 'highlightedColumnMarker' ("selected my-item") [optional]

        * downloadActionInViewMode - enable additional actions column in view mode with download button.
        * actionIsFirstColumn - moving action column to left
        * duplicateButton - add duplicate button
-->

<#-- TODO:
        - rewrite control on knockout.component
-->


<!-- ko ifnot: empty -->
<div class="view-table-container" style="<#if params.maxheight??>max-height: ${params.maxheight};</#if>">
    <table>
        <thead data-bind="with: singleValue">
            <tr data-bind="if: $data.impl">
                <#if actionIsFirstColumn == "true">
                    <@actionsHeader />
                </#if>
                <!-- ko with: impl -->
                    <#if params.journalType??>
                        <!-- ko with: new koutils.koclass("JournalType")("${params.journalType}") -->
                            <!-- ko foreach: defaultAttributes -->
                                <th data-bind="text: displayName"></th>
                            <!-- /ko -->
                        <!-- /ko -->
                    <#elseif columns??>
                        <#list columns?split(",") as column>
                            <!-- ko with: attribute("${column}") -->
                                <#if has_column_title_formatter(column) >
                                    <th data-bind="html: ko.computed(function() {
                                        <@column_title_formatter column 1 />
                                    })"></th>
                                <#else>
                                    <th data-bind="text: ko.computed(function() {
                                        return $parent.attribute('${column}').title() ?
                                            $parent.attribute('${column}').title() : Alfresco.util.message('no-title');
                                    })"></th>
                                </#if>
                            <!-- /ko -->
                        </#list>
                    <#else>
                        <!-- ko foreach: attributes -->
                            <th data-bind="text: title, attr: { id: name }"></th>
                        <!-- /ko -->
                    </#if>
                <!-- /ko -->

                <#if actionIsFirstColumn != "true">
                    <@actionsHeader />
                </#if>

            </tr>
        </thead>
        <tbody data-bind="foreach: multipleValues">
            <!-- ko if: $data.impl -->
                <!-- ko with: impl -->
                    <#if params.highlightedColumnMarker??>
                        <tr class="value-item"
                            data-bind="css: {
                                                    'highlighted ${params.highlightedAdditionalClass!""}': ko.computed(function() {
                                                        if ($data.attribute('${params.highlightedColumnMarker?js_string}'))
                                                            return $data.attribute('${params.highlightedColumnMarker?js_string}').value();
                                                        return false;
                                                    })
                                                }">
                    <#else>
                        <tr class="value-item">
                    </#if>
                        <#if actionIsFirstColumn == "true">
                            <@actionsBody />
                        </#if>
                        <#if params.journalType??>
                            <!-- ko with: new koutils.koclass("JournalType")("${params.journalType}") -->
                                <!-- ko foreach: defaultAttributes -->
                                    <!-- ko with: $parents[1].attribute($data.name()) -->
                                        <td data-bind="text: ko.computed(function() {
                                                             var value = $data.value(), title;
                                                             if (value && value.toString().indexOf('invariants.Node') != -1) {
                                                                 title = value.properties ? value.properties['cm:title'] : null;
                                                             }
                                                             return title || ($data.valueTitle() || $data.textValue())
                                            })"></td>
                                    <!-- /ko -->
                                <!-- /ko -->
                            <!-- /ko -->
                        <#elseif columns??>
                            <#list columns?split(",") as column>
                                <!-- ko with: attribute("${column}") -->
                                    <#if has_column_formatter(column) >
                                        <td data-bind="html: ko.computed(function() {
                                            <@column_formatter column 1 />
                                            })"></td>
                                    <#else>
                                        <td data-bind="text: ko.computed(function() {
                                                            var value = $data.value(), title;
                                                            if (value && value.toString().indexOf('invariants.Node') != -1) {
                                                                title = value.properties ? value.properties['cm:title'] : null;
                                                            }
                                                            return title || ($data.valueTitle() || $data.textValue())
                                            })"></td>
                                    </#if>
                                <!-- /ko -->
                            </#list>
                        <#else>
                            <!-- ko foreach: attributes -->
                                <td data-bind="text: ko.computed(function() {
                                                     var value = $data.value(), title;
                                                     if (value && value.toString().indexOf('invariants.Node') != -1) {
                                                          title = value.properties['cm:title'];
                                                     }
                                                     return title || ($data.valueTitle() || $data.textValue())
                                    })"></td>
                            <!-- /ko -->
                        </#if>
                        <#if actionIsFirstColumn != "true">
                            <@actionsBody />
                        </#if>
                    </tr>
                <!-- /ko -->
            <!-- /ko -->
        </tbody>
    </table>
</div>
<!-- /ko -->

<!-- ko if: empty -->
    <span>${msg("label.none")}</span>
<!-- /ko -->

<#if params.style??>
    <style type="text/css">
        ${params.style?string}
    </style>
</#if>

<#macro column_formatter columnName step>
    <#assign formatter = params['formatter_' + columnName?replace(':', '_')]!''>
    <#if formatter?has_content>return (function(value) { ${formatter} })($parents[${step}]);</#if>
</#macro>

<#macro column_title_formatter columnName step>
    <#assign title_formatter = params['title_formatter_' + columnName?replace(':', '_')]!''>
    <#if title_formatter?has_content>return (function(value) { ${title_formatter} })($parents[${step}]);</#if>
</#macro>

<#macro actionsHeader>
    <!-- ko with: $parent -->
        <!-- ko ifnot: protected() || resolve("node.impl.inViewMode") -->
            <th class="value-item-actions">${msg('view-table.labels.actions')}</th>
        <!-- /ko -->
        <!-- ko if: resolve("node.impl.inViewMode") -->
            <#if downloadActionInViewMode == "true">
                <th class="value-item-actions">${msg('view-table.labels.actions')}</th>
            </#if>
        <!-- /ko -->
    <!-- /ko -->
</#macro>

<#macro actionsBody>
    <!-- ko ifnot: $parents[1].protected() || $parents[1].resolve("node.impl.inViewMode") -->
        <td class="value-item-actions">
            <a class="edit-value-item" title="${msg('button.edit')}"
                data-bind="click: Citeck.forms.dialog.bind(Citeck.forms, $data.nodeRef(), null, function() { $data.reset(true) },
                    {
                        baseRef: $parents[1].resolve('node.impl.nodeRef') || '',
                        rootAttributeName: <#if globalAttributeName??>'${globalAttributeName}'<#else>null</#if>
                    }), clickBubble: false"></a>
            <#if duplicateButton == "true">
                <a class="duplicate-value-item" title="${msg('button.duplicate')}"
                    data-bind="click: Citeck.forms.duplicateValue.bind(null, $data, $parents[1]), clickBubble: false"></a>
            </#if>
            <a class="delete-value-item" title="${msg('button.delete')}"
                data-bind="click: function() {
                    Citeck.forms.simpleDeleteDialog(function() { ($parents[1].remove.bind($parents[1], $index()))() })
                }, clickBubble: false"></a>
        </td>
    <!-- /ko -->
    <!-- ko if: $parents[1].resolve("node.impl.inViewMode") -->
        <#if downloadActionInViewMode == "true">
            <td class="value-item-actions">
                <!-- ko if: $data.node().properties['cm:content'] -->
                    <a class="download-content-item" title="${msg('actions.document.download')}"
                        data-bind="click: function() {
                            document.location.href = Alfresco.constants.PROXY_URI + '/citeck/print/content?nodeRef=' + $data.nodeRef();
                        }, clickBubble: false"></a>
                <!-- /ko -->
            </td>
        </#if>
    <!-- /ko -->
</#macro>

<#function has_column_formatter columnName>
    <#assign formatter = params['formatter_' + columnName?replace(':', '_')]!''>
    <#return formatter?has_content />
</#function>

<#function has_column_title_formatter columnName>
    <#assign title_formatter = params['title_formatter_' + columnName?replace(':', '_')]!''>
    <#return title_formatter?has_content />
</#function>