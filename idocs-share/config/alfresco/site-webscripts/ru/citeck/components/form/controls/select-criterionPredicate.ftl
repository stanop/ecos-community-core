<#include "/ru/citeck/components/form/controls/select-macro.ftl" />

<script type="text/javascript">//<![CDATA[
<#assign params = field.control.params />

<#--
    Example for renderField:
        "template": "/ru/citeck/components/form/controls/select-criterionPredicate.ftl"

-->

(function() {

    // Definition the QName
    var qnameFull = null;
    <#if params.qnameFull??>qnameFull = "${params.qnameFull}";</#if>
    if (!qnameFull) {
        console.dir("ERROR: the qName-Full value is not defined!");
        qnameFull = "";
    }
    var attrFullName = qnameFull.replace('{', '%7B').replace('}', '%7D');

    // function, that creates the control
    var createSelectControl = function(datatype) {
        var dt = (datatype) ? datatype : "";
        var select = new Alfresco.SelectControl("${fieldHtmlId}").setOptions({
            optionsUrl: "${url.context}/page/search/search-predicates?datatype=" + dt,
            mode: "${form.mode}",
            <#if field.value??>originalValue: "${field.value?js_string}",</#if>
            <#if params.selectedItem??>selectedItem: "${params.selectedItem}",</#if>
            <#if params.responseType??>responseType: ${params.responseType},</#if>
            <#if params.responseSchema??>responseSchema: ${params.responseSchema},</#if>
            <#if params.requestParam??>requestParam: "${params.requestParam}",</#if>
            <#if params.titleField??>titleField: "${params.titleField}",</#if>
            <#if params.valueField??>valueField: "${params.valueField}",</#if>
            <#if params.sortKey??>sortKey: "${params.sortKey}",</#if>
            <#if params.resultsList??>resultsList: "${params.resultsList}",</#if>
        }).setMessages(${messages});
    }

    // Next, call the webscript and get datatype
    Alfresco.util.Ajax.request({
        url: "${url.context}/proxy/alfresco/search/search-attributes?attrFull=" + attrFullName,
        successCallback: {
            scope: this, fn: function(response) {
                if (!response || !response.json || !response.json.attributes) return;
                var attrs = response.json.attributes;
                var dt = null;
                for (var j = 0; j < attrs.length; j++) {
                    if (!attrs[j].fullName || !attrs[j].datatype) continue;
                    if (attrs[j].fullName === fldSelectValue) {
                        dt = attrs[j].datatype;
                        break;
                    }
                }
                if (!dt) {
                    console.dir("[select-criterionPredicate.ftl] datatype not exists!");
                    dt = "";
                }
                createSelectControl(dt);
            }
        },
        failureCallback: {},
        execScripts: true
    });
})();
//]]></script>

<@selectFieldHTML "${fieldHtmlId}" field/>
