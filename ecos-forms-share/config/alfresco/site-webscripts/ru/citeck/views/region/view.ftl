<#assign params = viewScope.region.params!{} />
<#assign isViewMode = (viewScope.view.mode == "view")/>
<#assign completeDelete = (params.completeDelete!"false") == "true" />

<!-- ko foreach: multipleValues -->
<span class="value-item">
    <#assign showLink = params.isLinked!(isViewMode?string) />

    <#if showLink == "true">
        <!-- ko if: $data instanceof koutils.koclass("invariants.Node") -->
             <a class="value-item-text" data-bind="text: $parent.getValueTitle($data), attr: { title: $parent.getValueDescription($data), href: $parent.href($data) }"></a>
        <!-- /ko -->

        <!-- ko ifnot: $data instanceof koutils.koclass("invariants.Node") -->
            <span class="value-item-text" data-bind="text: $parent.getValueTitle($data), attr: { title: $parent.getValueDescription($data) }"></span>
        <!-- /ko -->
    <#else>
        <span class="value-item-text" data-bind="text: $parent.getValueTitle($data), attr: { title: $parent.getValueDescription($data) }"></span>
    </#if>

    <#if !isViewMode>
        <span class="value-item-actions" data-bind="ifnot: $parent.protected()">
            <!-- ko if: $data instanceof koutils.koclass("invariants.Node") && $data.hasPermission("Write") -->
                <a class="edit-value-item" title="${msg('button.edit')}" data-bind="click: Citeck.forms.dialog.bind(Citeck.forms, $data.nodeRef, null, function(result) { result.impl().reset(true) }), clickBubble: false"></a>
            <!-- /ko -->

            <#if completeDelete>
                <!-- ko if: $data instanceof koutils.koclass("invariants.Node") && $data.hasPermission("Delete") -->
                    <a class="delete-value-item" title="${msg('button.delete')}" data-bind="click: $parent.destroy.bind($parent, $index(), $data.nodeRef), clickBubble: false"></a>
                <!-- /ko -->
                <!-- ko ifnot: $data instanceof koutils.koclass("invariants.Node") && $data.hasPermission("Delete") -->
                    <a class="delete-value-item" title="${msg('button.delete')}" data-bind="click: $parent.remove.bind($parent, $index()), clickBubble: false"></a>
                <!-- /ko -->
            <#else>
                <a class="delete-value-item" title="${msg('button.delete')}" data-bind="click: $parent.remove.bind($parent, $index()), clickBubble: false"></a>
            </#if>
        </span>
    </#if>
</span>
<!-- /ko -->

<!-- ko if: empty -->
    <span>${msg(params.emptyLabel!"label.none")}</span>
<!-- /ko -->