<#assign params = viewScope.region.params!{} />

<#-- Parametes:
        * journalType - columns is defaultAttributes ("files-numenclature") [optional]
        * columns     - attributes name on string ("cm:name,tk:type") [optional]
        * maxheight   - maximum height of view-table-container ("150px", "50%") [optional]
        * style       - individual styles. added in the end of control (".style { font-size: 13px; }") [optional]
        
        * highlightedColumnMarker    - marked rows as 'highlighted' if specified attribute exists and 'true' ("cm:content") [optional]
        * highlightedAdditionalClass - additional classes for highlighted rows. use only with 'highlightedColumnMarker' ("selected my-item") [optional]
-->

<#-- TODO:
        - rewrite control on knockout.component
-->


<!-- ko ifnot: empty -->
    <div class="view-table-container" style="<#if params.maxheight??>max-height: ${params.maxheight};</#if>">
        <table>
            <thead data-bind="with: singleValue">
                <tr data-bind="if: $data.impl">
                    <!-- ko with: impl -->
                        <#if params.journalType??>
                            <!-- ko with: new koutils.koclass("JournalType")("${params.journalType}") -->
                                <!-- ko foreach: defaultAttributes -->
                                    <th data-bind="text: displayName"></th>
                                <!-- /ko -->
                            <!-- /ko -->
                        <#elseif params.columns??>
                            <!-- ko foreach: "${params.columns}".split(",") -->
                                <!-- ko if: $parent.attribute($data) -->
                                    <th data-bind="text: $parent.attribute($data).title"></th>
                                <!-- /ko -->

                                <!-- ko ifnot: $parent.attribute($data) -->
                                    <th>${msg('no-title')}</th>
                                <!-- /ko -->
                            <!-- /ko -->
                        <#else>
                            <!-- ko foreach: attributes -->
                                <th data-bind="text: title, attr: { id: name }"></th>
                            <!-- /ko -->
                        </#if>
                    <!-- /ko -->

                    <!-- ko with: $parent -->
                        <!-- ko ifnot: protected() || resolve("node.impl.inViewMode") -->
                            <th class="value-item-actions">${msg('view-table.labels.actions')}</th>
                        <!-- /ko -->
                    <!-- /ko -->
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

                            <#if params.journalType??>
                                <!-- ko with: new koutils.koclass("JournalType")("${params.journalType}") -->
                                    <!-- ko foreach: defaultAttributes -->
                                        <!-- ko with: $parents[1].attribute($data.name()) -->
                                            <td data-bind="text: ko.computed(function() {
                                                <@column_formatter column 3 />
                                                
                                                var value = $data.value(), title;
                                                if (value && value.toString().indexOf('invariants.Node') != -1) {
                                                    title = value.properties['cm:title'];
                                                }
                                                return title || ($data.valueTitle() || $data.textValue())
                                            })"></td>
                                        <!-- /ko -->
                                    <!-- /ko -->
                                <!-- /ko -->
                            <#elseif params.columns??>
                                <#list params.columns?split(",") as column>
                                    <!-- ko with: attribute("${column}") -->
                                        <td data-bind="text: ko.computed(function() {
                                            <@column_formatter column 1 />

                                            var value = $data.value(), title;
                                            if (value && value.toString().indexOf('invariants.Node') != -1) {
                                                title = value.properties['cm:title'];
                                            }
                                            return title || ($data.valueTitle() || $data.textValue())
                                        })"></td>
                                    <!-- /ko -->
                                </#list>
                            <#else>
                                <!-- ko foreach: attributes -->
                                    <td data-bind="text: ko.computed(function() {
                                        <@column_formatter column 1 />

                                        var value = $data.value(), title;
                                        if (value && value.toString().indexOf('invariants.Node') != -1) {
                                            title = value.properties['cm:title'];
                                        }
                                        return title || ($data.valueTitle() || $data.textValue())
                                    })"></td>
                                <!-- /ko -->
                            </#if>

                            <!-- ko ifnot: $parents[1].protected() || $parents[1].resolve("node.impl.inViewMode") -->
                                <td class="value-item-actions">
                                    <a class="edit-value-item" title="${msg('button.edit')}" 
                                        data-bind="click: Citeck.forms.dialog.bind(Citeck.forms, $data.nodeRef(), null, function() { $data.reset(true) }), clickBubble: false"></a>
                                    <a class="delete-value-item" title="${msg('button.delete')}" 
                                        data-bind="click: function() { 
                                            Citeck.forms.simpleDeleteDialog(function() { ($parents[1].remove.bind($parents[1], $index()))() })
                                        }, clickBubble: false"></a>
                                </td>
                            <!-- /ko -->
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