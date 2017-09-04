<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<#assign sd = false />
<#assign registrator = false />
<#if form.arguments.formId??>
    <#assign sd = form.arguments.formId?contains("sd") />
    <#assign registrator = form.arguments.formId?contains("registrator") />
</#if>

<@forms.setMandatoryFields
fieldNames = [
    "prop_tk_kind",
    "prop_idocs_registrationNumber",
    "prop_idocs_registrationDate",
    "assoc_idocs_signatory",
    "prop_orders_header",
    "prop_orders_expirationDate"
]/>

<@forms.fileUploadSupport />

<script type="text/javascript">//<![CDATA[
Citeck.forms.displayConditional("registration", "prop_idocs_documentStatus == 'onRegistration'", ["${args.htmlid}_prop_idocs_documentStatus"]);

YAHOO.Bubbling.on("renderCurrentValue", function(layer, args) {
    var control = args[1].eventGroup;
    // react only on file code
    //alert(control.id);
    if(!control.id.match("assoc_orders_fileCode")) {
        return;
    }
    var fieldId = control.id.replace(/^(.*assoc_orders_fileCode).*$/, "$1");
    var assoc = Dom.get(fieldId);
    var assoc2 = Dom.get(control.id+"-added");
    var nodeRef ="";
    if(control.currentSingleSelectedItem)
    nodeRef = control.currentSingleSelectedItem.nodeRef;
    var nodeRef2 =  assoc2.value;
    var nodeRef3 =  assoc.value;
    // append only if file code selected
    if(!nodeRef) {
        return;
    }
    else
    {
        var fileIndexDesrc = Dom.get(fieldId);

        var fieldCreationDateId = fieldId.replace("assoc_orders_fileCode", "prop_orders_creationDate");
        var creationDate = document.getElementById(fieldCreationDateId);
        if(creationDate)
        {
            var creationDateValue = creationDate.value;
            var creationDateObj = new Date(creationDateValue);
            
        }
        if(!creationDateValue)
        {
            var url = document.location.href;
            var end = url.indexOf('&')||url.length;
            var start = url.indexOf('nodeRef=');
            var node = url.substring(start, end);
            var searchUrl1 = Alfresco.constants.PROXY_URI + "citeck/node?"+node;
            var request1 = new XMLHttpRequest();
            request1.open('GET', searchUrl1, false);  // `false` makes the request synchronous
            request1.send(null);
            if (request1.status === 200) {
                if (request1.responseText)
                {
                    var data1 = eval('(' + request1.responseText + ')');
                    creationDateValueString = data1.props['orders:creationDate'];
                    var dt  = creationDateValueString.substring(0,2); 
                    var mon = creationDateValueString.substring(3,5); 
                    var yr  = creationDateValueString.substring(6,10);
                    var creationDateObj = new Date(yr, mon-1, dt);
                }
            }
        }
        

        var fieldDate = fieldId.replace("assoc_orders_fileCode", "prop_orders_expirationDate-cntrl-date");
        var pickerField = fieldId.replace("assoc_orders_fileCode", "prop_orders_expirationDate-cntrl");
        var fieldDateFull = fieldId.replace("assoc_orders_fileCode", "prop_orders_expirationDate");
        var picker = Alfresco.util.ComponentManager.get(pickerField);
        var dateDesrc = Dom.get(fieldDate);
        var dateFullDesrc = Dom.get(fieldDateFull);

        var searchUrl = Alfresco.constants.PROXY_URI + "citeck/node?nodeRef="+nodeRef;
        var request = new XMLHttpRequest();
        request.open('GET', searchUrl, false);  // `false` makes the request synchronous
        request.send(null);
        if (request.status === 200) {
            if (request.responseText)
            {
                var data = eval('(' + request.responseText + ')');
                var storagePeriod = data.props['dms:storagePeriod'];
            }
            if(dateDesrc && dateFullDesrc)
            {
                creationDateObj.setMonth(creationDateObj.getMonth() + storagePeriod);
                dateDesrc.value = creationDateObj.toString(picker._msg("form.control.date-picker.entry.date.format"));
                dateFullDesrc.value = Alfresco.util.toISO8601(creationDateObj, {"milliseconds":true});
            }
        }
    }
}, this);

//]]></script>

<#if formUI == "true">
    <@formLib.renderFormsRuntime formId=formId />
</#if>

<#if form.mode == "view">
    <#assign twoColumnClass = "yui-g plain" />
    <#assign threeColumnClass = "yui-gb plain" />
<#else>
    <#assign twoColumnClass = "yui-g" />
    <#assign twoColumnClass = "yui-g" />
    <#assign threeColumnClass = "yui-gb" />
</#if>

<@formLib.renderFormContainer formId=formId>

    <#if form.mode == "view">
        <@forms.renderField field="prop_idocs_documentStatus" />
    </#if>
    <@forms.renderField field="prop_tk_kind" extension = {
        "endpointType": "d:category",
        "control" : {
            "template" : "/ru/citeck/components/form/controls/select.ftl",
            "params": {
                "optionsUrl": "${url.context}/proxy/alfresco/citeck/subcategories?nodeRef=workspace://SpacesStore/orders-cat-doctype-internal",
                "titleField": "name",
                "valueField": "nodeRef",
                "responseType": "YAHOO.util.DataSource.TYPE_JSON",
                "responseSchema": "{ resultsList: 'nodes', fields: [ {key:'nodeRef'}, {key:'name'} ] }"
            }
        }
    } />

    <#if sd>
        <@forms.renderField field="prop_orders_fingerboard" />
    <#else>
        <@forms.renderField field="prop_orders_fingerboard" extension = {
            "control": {
                "template": "/org/alfresco/components/form/controls/info.ftl"
            }
        }/>
    </#if>

    <@forms.renderField field="prop_orders_projectNumber" extension = {
        "control": {
            "template": "/org/alfresco/components/form/controls/info.ftl"
        }
    }/>

    <@forms.renderField field="prop_orders_creationDate" extension = {
        "control": {
            "template": "/org/alfresco/components/form/controls/info.ftl"
        }
    }/>

<div id="registration">

    <#if sd>
        <@forms.renderField field="prop_idocs_registrationNumber"  extension = {
            "control": {
                "template": "/ru/citeck/components/form/controls/auto-manual.ftl",
                "params": {
                }
            }
        }/>
    <#else>
        <@forms.renderField field="prop_idocs_registrationNumber" extension = {
            "control": {
                "template": "/org/alfresco/components/form/controls/info.ftl"
            }
        }/>
    </#if>

    <@forms.renderField field="prop_idocs_registrationDate" extension = {
        "control": {
            "template": "/org/alfresco/components/form/controls/info.ftl"
        }
    }/>
</div>

    <@forms.renderField field="assoc_idocs_registrator" extension = {
        "disabled": true,
        "control": {
            "template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
            "params": {
                "searchQuery" : "user=true&default=false"
            }
        }
    }/>


    <@forms.renderField field="assoc_idocs_signatory" extension = {
        "control": {
            "template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
            "params": {
                "searchQuery" : "user=true&default=false"
            }
        }
    }/>

    <@forms.renderField field="assoc_idocs_initiator" extension = {
        "disabled": false,
        "control": {
            "template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
            "params": {
                "searchQuery" : "user=true&default=false"
            }
        }
    }/>

<#--<#if registrator>
    <@forms.renderField field="assoc_orders_fileCode" extension = {
        "mandatory": true,
        "control": {
            "template": "/ru/citeck/components/form/controls/association_search.ftl",
            "params": {
                "searchWholeRepo": "true"
            }
        }
    }/>
<#else>
    <@forms.renderField field="assoc_orders_fileCode" extension = {
        "control": {
            "template": "/org/alfresco/components/form/controls/info.ftl"
        }
    }/>
</#if>-->

    <@forms.renderField field="assoc_orders_fileCode" />

    <@forms.renderField field="assoc_orders_branch" extension = {
        "disabled": false,
        "control": {
            "template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
            "params": {
                "searchQuery" : "branch=true&default=false"
            }
        }
    }/>

    <@forms.renderField field="prop_orders_header" />

    <#if registrator>
        <@forms.renderField field="prop_idocs_summary" extension = {
            "control": {
                "template": "/org/alfresco/components/form/controls/textarea.ftl",
                "params": {}
            }
        }/>
    <#else>
        <@forms.renderField field="prop_idocs_summary" extension = {
            "control": {
                "template": "/org/alfresco/components/form/controls/textarea.ftl",
                "params": {}
            },
            "disabled": true
        }/>
    </#if>

    <#if registrator>
        <@forms.renderField field="prop_orders_expirationDate" />
    <#else>
        <@forms.renderField field="prop_orders_expirationDate" extension = {
            "control": {
                "template": "/org/alfresco/components/form/controls/info.ftl"
            }
        }/>
    </#if>

    <@forms.renderField field="prop_orders_appendixNumber" />

    <@forms.renderField field="prop_orders_placeOfOriginal" />

</@>
