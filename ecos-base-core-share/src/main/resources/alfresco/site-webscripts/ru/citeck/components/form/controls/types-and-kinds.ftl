<#assign typeFieldId = field.control.params.typeFieldId!"prop_tk_type" />
<#assign kindFieldId = field.control.params.kindFieldId!"prop_tk_kind" />

<div id="types-and-kinds-field" class="form-field types-and-kinds">
    <#if form.mode == "view">
        <div class="viewmode-field type">
            <span class="viewmode-label">${msg("types-and-kinds.type")}:</span>
            <span class="viewmode-value"></span>
        </div>
        <div class="viewmode-field kind">
            <span class="viewmode-label">${msg("types-and-kinds.kind")}:</span>
            <span class="viewmode-value"></span>
        </div>
        <script type="text/javascript">
            new Citeck.widget.TypesAndKindsView("types-and-kinds-field").setOptions({
                    preType: <#if form.data[typeFieldId]??>"${form.data[typeFieldId]?js_string}"<#else>null</#if>,
                    preKind: <#if form.data[kindFieldId]??>"${form.data[kindFieldId]?js_string}"<#else>null</#if>,

                    // hidden fields (hidden: "type", "kind", "both")
                    hidden: <#if field.control.params.hidden??>"${field.control.params.hidden?js_string}"<#else>null</#if>
            }).setMessages(${messages});
        </script>
    <#else>
        <div class="first-select-control select-control">
            <label for="${args.htmlid?html}_${typeFieldId}">
                ${msg("types-and-kinds.type")}:
                <#if field.control.params.mandatory?? && (field.control.params.mandatory == "both" || field.control.params.mandatory == "type")>
                    <span class="mandatory-indicator">*</span>
                </#if>
            </label>
            <select name="${typeFieldId}" id="${args.htmlid?html}_${typeFieldId}" class="type">
                <#if field.control.params.blank?? && (field.control.params.blank == "both" || field.control.params.blank == "type")>
                    <#if field.control.params.mandatory?? && (field.control.params.mandatory == "both" || field.control.params.mandatory == "type")>
                        <option value="" disabled="true">${msg("types-and-kinds.select-type")}</option>
                    <#else>
                        <option value="">${msg("types-and-kinds.select-type")}</option>
                    </#if>
                </#if>
            </select>
        </div>

        <div class="second-select-control select-control">
            <label for="${args.htmlid?html}_${kindFieldId}">
                ${msg("types-and-kinds.kind")}:
                <#if field.control.params.mandatory?? && (field.control.params.mandatory == "both" || field.control.params.mandatory == "kind")>
                    <span class="mandatory-indicator">*</span>
                </#if>
            </label>
            <select name="${kindFieldId}" id="${args.htmlid?html}_${kindFieldId}" class="kind">
                <#if field.control.params.blank?? && (field.control.params.blank == "both" || field.control.params.blank == "kind")>
                    <#if field.control.params.mandatory?? && (field.control.params.mandatory == "both" || field.control.params.mandatory == "kind")>
                        <option value="" disabled="true">${msg("types-and-kinds.select-kind")}</option>
                    <#else>
                        <option value="">${msg("types-and-kinds.select-kind")}</option>
                    </#if>
                </#if>   
            </select>
        </div>
        <script type="text/javascript">
            new Citeck.widget.TypesAndKinds("types-and-kinds-field").setOptions({
                // root node (rootNode: "nodeRef")
                rootNode: <#if field.control.params.rootNode??>"${field.control.params.rootNode?js_string}"<#else>null</#if>,

                // hidden fields (hidden: "type", "kind", "both")
                hidden: <#if field.control.params.hidden??>"${field.control.params.hidden?js_string}"<#else>null</#if>,

                // mandatory fields. (mandatory: "type", "kind", "both")
                mandatory: <#if field.control.params.mandatory??>"${field.control.params.mandatory?js_string}"<#else>null</#if>,

                // include blank before options (blank: "type", "kind", "both")
                blank: <#if field.control.params.blank??>"${field.control.params.blank?js_string}"<#else>null</#if>,

                // lock element of type (fixedTypeOption: "nodeRef")
                fixedTypeOption: <#if field.control.params.fixedTypeOption??>"${field.control.params.fixedTypeOption?js_string}"<#else>null</#if>,

                // lock element of kind(fixedKindOption: "nodeRef")
                fixedKindOption: <#if field.control.params.fixedKindOption??>"${field.control.params.fixedKindOption?js_string}"<#else>null</#if>,

                // list of types (onlyTypes: "noderef,noderef...")
                onlyTypes: <#if field.control.params.onlyTypes??>"${field.control.params.onlyTypes?js_string}"<#else>null</#if>,

                // list of kinds (onlyKind: "noderef,noderef...")
                onlyKinds: <#if field.control.params.onlyKinds??>"${field.control.params.onlyKinds?js_string}"<#else>null</#if>,

                // recurse search and view (recurse: "true" or "false")
                recurse: <#if field.control.params.recurse??>"${field.control.params.recurse?js_string}"<#else>null</#if>,

                // For Edit Form
                preType: <#if form.data[typeFieldId]??>"${form.data[typeFieldId]?js_string}"<#else>null</#if>,
                preKind: <#if form.data[kindFieldId]??>"${form.data[kindFieldId]?js_string}"<#else>null</#if>
            }).setMessages(${messages});
        </script>
    </#if>
</div>