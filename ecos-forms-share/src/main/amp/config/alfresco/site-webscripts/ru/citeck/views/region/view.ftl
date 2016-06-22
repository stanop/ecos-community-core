<#assign params = viewScope.region.params!{} />
<#assign isViewMode = (viewScope.view.mode == "view")/>
<!-- ko foreach: multipleValues -->
<span class="value-item">
    <#assign showLink = params.isLinked!(isViewMode?string) />

    <#if showLink == "true">
        <!-- ko if: $data instanceof koutils.koclass("invariants.Node") -->
        <a class="value-item-text" data-bind="text: $parent.getValueTitle($data), attr: { title: $parent.getValueDescription($data), href: Alfresco.util.siteURL('document-details?nodeRef='+$data.nodeRef)}"></a>
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
            <a class="edit-value-item" title="${msg('button.edit')}" data-bind="click: Citeck.forms.dialog.bind(Citeck.forms, $data.nodeRef, null, function(){}), clickBubble: false"></a>
            <!-- /ko -->
            <a class="delete-value-item" title="${msg('button.delete')}" data-bind="click: $parent.remove.bind($parent, $index()), clickBubble: false"></a>
        </span>
    </#if>
</span>
<!-- /ko -->

<!-- ko if: empty -->
    <span>${msg(params.emptyLabel!"label.none")}</span>
<!-- /ko -->