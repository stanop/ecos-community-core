<#assign saveAndLoadTemplate = field.control.params.saveAndLoadTemplate!"false">

<#assign routeObjectFieldId = field.control.params.routeObjectFieldId!"route_object_field">
<#assign participantListFieldId = field.control.params.participantListFieldId!"participant_list_field">
<#assign priorityFieldId = field.control.params.priorityFieldId!"prop_route_precedence">


<div id="route-field" class="form-field route ${form.mode?string}">
    <#if form.mode == "view">
        <div class="viewmode-field">
            <div class="viewmode-label">
                ${msg("route.stages")}:
            </div>
            <div class="viewmode-value">
                <table class="stages">
                    <thead>
                        <th>${msg("route.stage")}</th>
                        <th>${msg("route.time")}</th>
                        <th>${msg("route.participants")}</th>
                    </thead>
                    <tbody></tbody>
                </table>
            </div>
        </div>
    <#else>
        <table class="stages"></table>
    </#if>

    <#if form.mode != "view">
        <div class="buttons">
            <button class="addStage">${msg("route.add-stage")}</button>
            <#if saveAndLoadTemplate == "true">
                <button class="saveAsTemplateDialog">${msg("route.save-as-template")}</button>
                <button class="loadTemplateDialog">${msg("route.load-from-template")}</button>
            </#if>
        </div>

        <#if saveAndLoadTemplate == "true">
            <div id="saveAsTemplatePanel">
                <div class="hd">${msg("route.new-route")}</div>
                <div class="bd">
                    <div class="share-form">
                        <div class="form-field">
                            <label for="routeName">${msg("route.route-name")}:</label>
                            <input type="text" id="routeName" name="routeName">
                        </div>                    
                    </div>
                </div>
                <div class="ft"></div>
            </div>

            <div id="loadTemplatePanel">
                <div class="hd">${msg("route.load-route")}</div>
                <div class="bd">
                    <div class="share-form">
                        <div class="form-field">
                            <label for="routeTemplate">${msg("route.route-name")}:</label>
                            <select id="routeTemplate" name="routeTemplate"></select>
                        </div>                    
                    </div>
                </div>
                <div class="ft"></div>
            </div>
        </#if>
    </#if>

    <#if form.mode != "create">
        <input type="hidden" id="nodeRefItemId" name="nodeRefItemId" value="${form.arguments.itemId}">
    </#if>

    <input type="hidden" id="${args.htmlid?html}_${routeObjectFieldId}" name="${routeObjectFieldId}" value="${form.data[routeObjectFieldId]!""}">
    <input type="hidden" id="${args.htmlid?html}_${participantListFieldId}" name="${participantListFieldId}" value="${form.data[participantListFieldId]!""}">
    <input type="hidden" id="${args.htmlid?html}_${priorityFieldId}" name="${priorityFieldId}" value="${form.data[priorityFieldId]!""}">

    <input type="hidden" id="${args.htmlid?html}_field_name" name="field_names" value="${routeObjectFieldId},${participantListFieldId},${priorityFieldId}">
</div>

<script type="text/javascript">
    var citeckWidgetRoute = new Citeck.widget.Route("route-field").setOptions({
        // Pre-installed template (nodeRef)
        <#if field.control.params.presetTemplate??>
            presetTemplate: "${field.control.params.presetTemplate}",
        </#if>

        // Lock template. The user will not be able to delete stages and participants of pre-installed template
        // true or false. By default false
        <#if field.control.params.presetTemplateMandatory??>
            presetTemplateMandatory: "${field.control.params.presetTemplateMandatory}",
        </#if>

        // Which type of node are available for selection
        // The string contains the node type. "USER, GROUP"
        // By default "USER"
        <#if field.control.params.allowedAuthorityType??>
            allowedAuthorityType: "${field.control.params.allowedAuthorityType}",
        </#if>

        // Which type of group are available for selection
        // The string contains the group type. "BRANCH, ROLE"
        // Parameter will by activated if "allowedAuthorityType" equal "GROUP" or "USER, GROUP"
        <#if field.control.params.allowedGroupType??>
            allowedGroupType: "${field.control.params.allowedGroupType}",
        </#if>

        // The possibility of on/off functions to save and load templates
        // true or false. By default false 
        saveAndLoadTemplate: "${saveAndLoadTemplate}",

        // Input id for array fo stages. String contains JSON
        routeObjectFieldId: "${args.htmlid?js_string}_${routeObjectFieldId?js_string}",

        // Input id for list of participants. String contains list of nodeRefs through ','
        participantListFieldId: "${args.htmlid?js_string}_${participantListFieldId?js_string}",

        // Input id for list of participants, grouped by stages. String contains list of nodeRefs through '|' and list of stages through ','
        priorityFieldId: "${args.htmlid?js_string}_${priorityFieldId?js_string}",
        
        mode: "${form.mode}",
        mandatory: ${field.mandatory?string}
    }).setMessages(${messages});
</script>