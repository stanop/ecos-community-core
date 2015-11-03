
<#-- Currency Directory -->

<#macro renderSoapWsCurrencyDirectoryPickerJS field picker="picker">

    var ${picker} = new Citeck.SoapWsCurrencyDirectory("${controlId}", "${fieldHtmlId}").setOptions({
        multipleSelectMode: ${field.endpointMany?string},
        dataSourceServiceUrl: "${field.control.params.dataSourceServiceUrl}",
        valuePropAlias: "${field.control.params.valuePropAlias}",
        namePropAlias: "${field.control.params.namePropAlias}",
        idPropAlias: "${field.control.params.idPropAlias}",
        field: "${field.name}",
        <#if field.mandatory??>
            mandatory: ${field.mandatory?string},
        <#elseif field.endpointMandatory??>
            mandatory: ${field.endpointMandatory?string},
        </#if>
        <#if field.control.params.valueType??>
            valueType: "${field.control.params.valueType}",
        </#if>
        selectActionLabel: "${field.control.params.selectActionLabel!msg("button.select")}",
        currentValue: "${field.value?js_string}",
        formMode: "${form.mode}"
    }).setMessages(
        ${messages}
    );

</#macro>

<#macro renderSoapWsCurrencyDirectoryPickerHTML controlId>
    <#assign pickerId = controlId + "-picker">

    <div id="${pickerId}" class="picker yui-panel">
        <div id="${pickerId}-head" class="hd">${msg("form.control.object-picker.header")}</div>
        <div id="${pickerId}-body" class="bd">
            <div class="picker-header"></div>
            <div class="yui-g">
                <div id="${pickerId}-left" class="yui-u first panel-left">
                    <div id="${pickerId}-results" class="picker-items">
                        <#nested>
                    </div>
                </div>
                <div id="${pickerId}-right" class="yui-u panel-right">
                    <div id="${pickerId}-selectedItems" class="picker-items"></div>
                </div>
            </div>
            <div class="bdft">
                <button id="${controlId}-ok" tabindex="0">${msg("button.ok")}</button>
                <button id="${controlId}-cancel" tabindex="0">${msg("button.cancel")}</button>
            </div>
        </div>
    </div>

</#macro>


<#-- Directory -->

<#macro renderSoapWsDirectoryPickerJS field picker="picker">

    var ${picker} = new Citeck.SoapWsDirectory("${controlId}", "${fieldHtmlId}").setOptions({
        multipleSelectMode: ${field.endpointMany?string},
        dataSourceServiceUrl: "${field.control.params.dataSourceServiceUrl}",
        valuePropAlias: "${field.control.params.valuePropAlias}",
        namePropAlias: "${field.control.params.namePropAlias}",
        field: "${field.name}",
        <#if field.mandatory??>
            mandatory: ${field.mandatory?string},
        <#elseif field.endpointMandatory??>
            mandatory: ${field.endpointMandatory?string},
        </#if>
        <#if field.control.params.valueType??>
            valueType: "${field.control.params.valueType}",
        </#if>
        selectActionLabel: "${field.control.params.selectActionLabel!msg("button.select")}",
        currentValue: "${field.value?js_string}",
        formMode: "${form.mode}"
    }).setMessages(
        ${messages}
    );

</#macro>

<#macro renderSoapWsDirectoryPickerHTML controlId>
    <#assign pickerId = controlId + "-picker">

    <div id="${pickerId}" class="picker yui-panel">
        <div id="${pickerId}-head" class="hd">${msg("form.control.object-picker.header")}</div>
        <div id="${pickerId}-body" class="bd">
            <div class="picker-header"></div>
            <div class="yui-g">
                <div id="${pickerId}-left" class="yui-u first panel-left">
                    <div id="${pickerId}-results" class="picker-items">
                        <#nested>
                    </div>
                </div>
                <div id="${pickerId}-right" class="yui-u panel-right">
                    <div id="${pickerId}-selectedItems" class="picker-items"></div>
                </div>
            </div>
            <div class="bdft">
                <button id="${controlId}-ok" tabindex="0">${msg("button.ok")}</button>
                <button id="${controlId}-cancel" tabindex="0">${msg("button.cancel")}</button>
            </div>
        </div>
    </div>

</#macro>


<#-- People Directory -->

<#macro renderSoapWsPeopleDirectoryPickerJS field picker="picker">

    var ${picker} = new Citeck.SoapWsPeopleDirectory("${controlId}", "${fieldHtmlId}").setOptions({
        multipleSelectMode: ${field.endpointMany?string},
        dataSourceServiceUrl: "${field.control.params.dataSourceServiceUrl}",
        valuePropAlias: "${field.control.params.valuePropAlias}",
        namePropAlias: "${field.control.params.namePropAlias}",
        field: "${field.name}",
        <#if field.mandatory??>
            mandatory: ${field.mandatory?string},
        <#elseif field.endpointMandatory??>
            mandatory: ${field.endpointMandatory?string},
        </#if>
        <#if field.control.params.valueType??>
            valueType: "${field.control.params.valueType}",
        </#if>
        selectActionLabel: "${field.control.params.selectActionLabel!msg("button.select")}",
        currentValue: "${field.value?js_string}",
        formMode: "${form.mode}"
    }).setMessages(
        ${messages}
    );

</#macro>

<#macro renderSoapWsPeopleDirectoryPickerHTML controlId>
    <#assign pickerId = controlId + "-picker">

    <div id="${pickerId}" class="picker yui-panel">
        <div id="${pickerId}-head" class="hd">${msg("form.control.object-picker.header")}</div>
        <div id="${pickerId}-body" class="bd">
            <div class="picker-header"></div>
            <div class="yui-g">
                <div id="${pickerId}-left" class="yui-u first panel-left">
                    <div id="${pickerId}-results" class="picker-items">
                        <#nested>
                    </div>
                </div>
                <div id="${pickerId}-right" class="yui-u panel-right">
                    <div id="${pickerId}-selectedItems" class="picker-items"></div>
                </div>
            </div>
            <div class="bdft">
                <button id="${controlId}-ok" tabindex="0">${msg("button.ok")}</button>
                <button id="${controlId}-cancel" tabindex="0">${msg("button.cancel")}</button>
            </div>
        </div>
    </div>

</#macro>


<#-- Landlord Directory -->

<#macro renderSoapWsLandlordDirectoryPickerJS field picker="picker">

    var ${picker} = new Citeck.SoapWsLandlordDirectory("${controlId}", "${fieldHtmlId}").setOptions({
        multipleSelectMode: ${field.endpointMany?string},
        dataSourceServiceUrl: "${field.control.params.dataSourceServiceUrl}",
        valuePropAlias: "${field.control.params.valuePropAlias}",
        field: "${field.name}",
        <#if field.mandatory??>
            mandatory: ${field.mandatory?string},
        <#elseif field.endpointMandatory??>
            mandatory: ${field.endpointMandatory?string},
        </#if>
        <#if field.control.params.valueType??>
            valueType: "${field.control.params.valueType}",
        </#if>
        selectActionLabel: "${field.control.params.selectActionLabel!msg("button.select")}",
        currentValue: "${field.value?js_string}",
        formMode: "${form.mode}"
    }).setMessages(
        ${messages}
    );

</#macro>

<#macro renderSoapWsLandlordDirectoryPickerHTML controlId>
    <#assign pickerId = controlId + "-picker">

    <div id="${pickerId}" class="picker yui-panel">
        <div id="${pickerId}-head" class="hd">${msg("form.control.object-picker.header")}</div>
        <div id="${pickerId}-body" class="bd">
            <div class="picker-header">
                <table style="border-width:0px;">
                    <tr>
                        <td valign="middle"> <input id="${pickerId}-search-term" size="60" type="text"/> </td>
                        <td valign="middle"> <button id="${controlId}-search" tabindex="0">${msg("button.search")}</button> </td>
                    </tr>
                </table>
            </div>
            <div class="yui-g">
                <div id="${pickerId}-left" class="yui-u first panel-left">
                    <div id="${pickerId}-results" class="picker-items">
                        <#nested>
                    </div>
                </div>
                <div id="${pickerId}-right" class="yui-u panel-right">
                    <div id="${pickerId}-selectedItems" class="picker-items"></div>
                </div>
            </div>
            <div class="bdft">
                <button id="${controlId}-ok" tabindex="0">${msg("button.ok")}</button>
                <button id="${controlId}-cancel" tabindex="0">${msg("button.cancel")}</button>
            </div>
        </div>
    </div>

</#macro>


<#-- Address Directory -->

<#macro renderSoapWsAddressDirectoryPickerJS field picker="picker">

    var ${picker} = new Citeck.SoapWsAddressDirectory("${controlId}", "${fieldHtmlId}").setOptions({
        getFIASAddressServiceUrl: "${field.control.params.getFIASAddressServiceUrl}",
        getKIAAddressServiceUrl: "${field.control.params.getKIAAddressServiceUrl}",
        setKIAAddressServiceUrl: "${field.control.params.setKIAAddressServiceUrl}",
        field: "${field.name}",
        <#if field.mandatory??>
            mandatory: ${field.mandatory?string},
        <#elseif field.endpointMandatory??>
            mandatory: ${field.endpointMandatory?string},
        </#if>
        <#if field.control.params.valueType??>
            valueType: "${field.control.params.valueType}",
        </#if>
        selectActionLabel: "${field.control.params.selectActionLabel!msg("button.select")}",
        currentValue: "${field.value?js_string}",
        formMode: "${form.mode}"
    }).setMessages(
        ${messages}
    );

</#macro>

<#macro renderSoapWsAddressDirectoryPickerHTML controlId>
    <#assign pickerId = controlId + "-picker">

    <div id="${pickerId}" class="picker yui-panel">
        <div id="${pickerId}-head" class="hd">${msg("addressPicker.title")}</div>
        <div id="${pickerId}-body" class="bd">
            <div class="picker-header">
                <table style="border-width:0px;">
                    <tr>
                        <td valign="middle"> <input id="${pickerId}-search-term" size="60" type="text"/> </td>
                        <td valign="middle"> <button id="${controlId}-search" tabindex="0">${msg("button.search")}</button> </td>
                    </tr>
                </table>
            </div>
            <div class="yui-g">
                <div id="${pickerId}-left" class="yui-u first panel-left">
                    <div style="text-align:left;">&nbsp; ${msg("addressPicker.lable.streets")}:</div>
                    <div id="${pickerId}-streets" class="picker-items">
                        <#nested>
                    </div>
                </div>
                <div id="${pickerId}-right" class="yui-u panel-right">
                    <div style="text-align:left;">${msg("addressPicker.lable.addresses")}:</div>
                    <div id="${pickerId}-addresses" class="picker-items"></div>
                </div>
            </div>
            <h2>
                &nbsp;&nbsp; ${msg("addressPicker.lable.address")}:
                <span id="${pickerId}-current-address-brief"></span>
            </h2>
            <div class="bdft">
                <button id="${controlId}-ok" tabindex="0">${msg("button.ok")}</button>
                <button id="${controlId}-cancel" tabindex="0">${msg("button.cancel")}</button>
            </div>
        </div>
    </div>

</#macro>

<#macro renderSoapWsCreateNewAddressDialogPickerHTML controlId>
    <#assign pickerId = controlId + "-create-address-dialog">

    <div id="${pickerId}" class="picker yui-panel">
        <div id="${pickerId}-head" class="hd">${msg("createAddressDialog.title")}</div>
        <div id="${pickerId}-body" class="bd">
            <div class="picker-header"></div>
            <div class="yui-g">
                <br/>
                <table style="border-width:0px;">
                    <tr>
                        <td valign="middle"> ${msg("createAddressDialog.field.street.title")} </td>
                        <td valign="middle">
                            <span id="${pickerId}-street-brief"></span>
                            <input id="${pickerId}-street" type="hidden"/>
                            <br/>
                        </td>
                    </tr>
                    <tr>
                        <td valign="middle"> ${msg("createAddressDialog.field.house.title")} </td>
                        <td valign="middle"> <input id="${pickerId}-house" type="text"/> </td>
                    </tr>
                    <tr>
                        <td valign="middle"> ${msg("createAddressDialog.field.bld.title")} </td>
                        <td valign="middle"> <input id="${pickerId}-bld" type="text"/> </td>
                    </tr>
                    <tr>
                        <td valign="middle"> ${msg("createAddressDialog.field.comment.title")} </td>
                        <td valign="middle"> <input id="${pickerId}-comment" type="text"/> </td>
                    </tr>
                </table>
                <br/>
            </div>
            <div class="bdft">
                <button id="${controlId}-create-ok" tabindex="0">${msg("createAddressDialog.button.create.title")}</button>
                <button id="${controlId}-create-cancel" tabindex="0">${msg("button.cancel")}</button>
            </div>
        </div>
    </div>

</#macro>