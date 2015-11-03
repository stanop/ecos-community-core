<#assign params = viewScope.region.params!{} />

<#-- Parametes:
        * journalType - columns is defaultAttributes ("files-numenclature") [optional]
        * columns     - attributes name on string ("cm:name,tk:type") [optional]
        * maxheight   - maximum height of view-table-container ("150px", "50%") [optional]
-->

<!-- ko ifnot: empty -->
    <div class="view-table-container" style="max-height: <#if params.maxheight??>${params.maxheight}</#if>;">
        <table>
            <thead data-bind="with: singleValue">
                <tr>
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

                    <!-- ko ifnot: $parents[1].inViewMode -->
                        <th class="value-item-actions">${msg('view-table.labels.actions')}</th>
                    <!-- /ko -->
                </tr>
            </thead>
            <tbody data-bind="foreach: multipleValues">
                <tr class="value-item">
                    <!-- ko with: impl -->
                        <#if params.journalType??>
                            <!-- ko with: new koutils.koclass("JournalType")("${params.journalType}") -->
                                <!-- ko foreach: defaultAttributes -->
                                    <!-- ko if: $parents[1].attribute($data.name()) -->
                                        <td data-bind="text: $parents[1].attribute($data.name()).value"></td>
                                    <!-- /ko -->
                                <!-- /ko -->
                            <!-- /ko -->
                        <#elseif params.columns??>
                            <!-- ko foreach: "${params.columns}".split(",") -->
                                <!-- ko if: $parent.attribute($data) -->
                                    <td data-bind="text: $parent.attribute($data).valueTitle"></td>
                                <!-- /ko -->
                            <!-- /ko -->
                        <#else>
                            <!-- ko foreach: attributes -->
                                <td data-bind="text: textValue"></td>
                            <!-- /ko -->
                        </#if>
                    <!-- /ko -->

                    <!-- ko ifnot: $parents[1].inViewMode -->
                        <td class="value-item-actions">
                            <a class="edit-value-item" title="${msg('button.edit')}" 
                               data-bind="click: Citeck.forms.dialog.bind(Citeck.forms, $data.nodeRef, null, function(){}), clickBubble: false"></a>
                            <a class="delete-value-item" title="${msg('button.delete')}" 
                               data-bind="click: $parent.remove.bind($parent, $index()), clickBubble: false"></a>
                        </td>
                    <!-- /ko -->
                </tr>
            </tbody>
        </table> 
    </div>
<!-- /ko -->

<!-- ko if: empty -->
    <span>${msg("label.none")}</span>
<!-- /ko -->