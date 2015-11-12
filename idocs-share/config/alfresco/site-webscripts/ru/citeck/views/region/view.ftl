<#assign params = viewScope.region.params!{} />
<#assign isViewMode = (viewScope.view.mode == "view")/>
<!-- ko ifnot: empty -->
<!-- ko foreach: multipleValues -->
<span class="value-item">
            <#if !params.isLinked??>
                <#assign showLink = isViewMode?string/>
            <#else>
                <#assign showLink = params.isLinked/>
            </#if>
            <#if showLink == "true">
                <!-- ko if: $data instanceof koutils.koclass("invariants.Node") -->
                <a class="value-item-text" data-bind="text: $parent.getValueTitle($data), attr: { title: $parent.getValueDescription($data), href: Alfresco.util.siteURL('card-details?nodeRef='+$data.nodeRef)}"></a>
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
<!-- /ko -->

<!-- ko if: empty -->
<span>${msg("label.none")}</span>
<!-- /ko -->