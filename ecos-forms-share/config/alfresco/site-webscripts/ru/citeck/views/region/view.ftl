<#assign params = viewScope.region.params!{} />
<#assign isViewMode = (viewScope.view.mode == "view")/>
<#assign completeDelete = (params.completeDelete!"false") == "true" />
<#assign editable>
    <#if config.scoped["InvariantControlsConfiguration"]?? &&
             config.scoped["InvariantControlsConfiguration"]["view"]?? &&
             config.scoped["InvariantControlsConfiguration"]["view"].attributes["editable"]??>
        ${config.scoped["InvariantControlsConfiguration"]["view"].attributes["editable"]}
    <#else>all</#if>
</#assign>
<#assign postprocessing = params.postprocessing!"">
<#assign asis><#if params.asis?? && params.asis == "true">white-space: pre-wrap;<#else></#if></#assign>


<#macro valueText isLinked="false">
    <#if isLinked == "true">
        <a class="value-item-text ${asis}" style="${asis}" 
            data-bind="text: $parent.getValueTitle($data), attr: { title: $parent.getValueDescription($data), href: $parent.href($data) }"></a>
    <#else>
        <#if postprocessing?has_content>
            <span class="value-item-text ${asis}" style="${asis}" 
                data-bind="text: $parent.getValueTitle($data, function(value) { return ${postprocessing}; }), attr: { title: $parent.getValueDescription($data) }"></span>
        <#else>
            <span class="value-item-text ${asis}" style="${asis}" 
                data-bind="text: $parent.getValueTitle($data), attr: { title: $parent.getValueDescription($data) }"></span>
        </#if>
    </#if>
</#macro>

<#if isViewMode && inlineEdit!false>
    <div class="view-value" data-bind="click: showEditableField, clickBubble: false">
</#if>

<!-- ko foreach: multipleValues -->
    <span class="value-item">
        <#assign showLink = params.isLinked!(isViewMode?string) />

        <#if showLink == "true">
            <!-- ko if: $data instanceof koutils.koclass("invariants.Node") -->
                <@valueText "true" />
            <!-- /ko -->

            <!-- ko ifnot: $data instanceof koutils.koclass("invariants.Node") -->
                <@valueText />
            <!-- /ko -->
        <#else>
            <@valueText />
        </#if>

        <#if isViewMode && inlineEdit!false>
            <!--ko if: $parent.inlineEditVisibility -->
        </#if>

        <#if !isViewMode || inlineEdit!false>
            <span class="value-item-actions" data-bind="ifnot: $parent.protected()">
        
            <#if editable?trim == "all" || (editable?trim == "admin" && user.isAdmin)>
                <!-- ko if: $data instanceof koutils.koclass("invariants.Node") && $data.hasPermission("Write") -->
                    <a class="edit-value-item" title="${msg('button.edit')}" data-bind="click: Citeck.forms.dialog.bind(Citeck.forms, $data.nodeRef, null, function(result) { result.impl().reset(true) }), clickBubble: false"></a>
                <!-- /ko -->
            </#if>

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

        <#if isViewMode && inlineEdit!false>
            <!-- /ko -->
        </#if>
    </span>
<!-- /ko -->

<!-- ko if: empty -->
    <span>${msg(params.emptyLabel!"label.none")}</span>
<!-- /ko -->

<#if isViewMode && inlineEdit!false>
    </div>
</#if>
