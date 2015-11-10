<#assign params = viewScope.region.params!{} />
<!-- ko ifnot: empty -->
    <!-- ko foreach: multipleValues -->
        <span class="value-item">
            <!-- ko if: $data instanceof koutils.koclass("invariants.Node") -->
            <#if !params.isLinked??>
                <!-- ko if: $parents[1].inViewMode() -->
                <a class="value-item-text" data-bind="text: $parent.getValueTitle($data), attr: { title: $parent.getValueDescription($data), href: Alfresco.util.siteURL('card-details?nodeRef='+$data.nodeRef)}"></a>
                <!-- /ko -->
                <!-- ko ifnot: $parents[1].inViewMode() -->
                <span class="value-item-text" data-bind="text: $parent.getValueTitle($data), attr: { title: $parent.getValueDescription($data) }"></span>
                <!-- /ko -->
            <#elseif params.isLinked=="true">
                <a class="value-item-text" data-bind="text: $parent.getValueTitle($data), attr: { title: $parent.getValueDescription($data), href: Alfresco.util.siteURL('card-details?nodeRef='+$data.nodeRef)}"></a>
            <#else>
                <span class="value-item-text" data-bind="text: $parent.getValueTitle($data), attr: { title: $parent.getValueDescription($data) }"></span>
            </#if>
            <!-- /ko -->
            <!-- ko ifnot: $data instanceof koutils.koclass("invariants.Node") -->
            <span class="value-item-text" data-bind="text: $parent.getValueTitle($data), attr: { title: $parent.getValueDescription($data) }"></span>
            <!-- /ko -->

            <span class="value-item-actions" data-bind="ifnot: $parents[1].inViewMode() || $parent.protected()">
                <!-- ko if: $data instanceof koutils.koclass("invariants.Node") && $data.hasPermission("Write") -->
                <a class="edit-value-item" title="${msg('button.edit')}" data-bind="click: Citeck.forms.dialog.bind(Citeck.forms, $data.nodeRef, null, function(){}), clickBubble: false"></a>
                <!-- /ko -->
                <a class="delete-value-item" title="${msg('button.delete')}" data-bind="click: $parent.remove.bind($parent, $index()), clickBubble: false"></a>
            </span>
       </span>
    <!-- /ko -->
<!-- /ko -->

<!-- ko if: empty -->
    <span>${msg("label.none")}</span>
<!-- /ko -->