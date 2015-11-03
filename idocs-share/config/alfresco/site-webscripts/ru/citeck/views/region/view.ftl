<!-- ko ifnot: empty -->
    <!-- ko foreach: multipleValues -->
        <span class="value-item">
            <span class="value-item-text" data-bind="text: $parent.getValueTitle($data), attr: { title: $parent.getValueDescription($data) }"></span>
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