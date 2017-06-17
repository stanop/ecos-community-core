<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields
fieldNames = [
	"prop_journal_fieldQName",
	"prop_journal_predicate"
]/>

<@forms.fileUploadSupport />

<@forms.renderFormsRuntime formId=formId />

<#if form.mode == "view">
	<#assign twoColumnClass = "yui-g plain" />
	<#assign threeColumnClass = "yui-gb plain" />
<#else>
	<#assign twoColumnClass = "yui-g" />
	<#assign threeColumnClass = "yui-gb" />
</#if>

<@formLib.renderFormContainer formId=formId>
    <input type="hidden" name="alf_assoctype" value="sys:children" />

    <#-- ## ## ## ## ## ## ## ## ## RENDERING FIELD: QNAME ## ## ## ## ## ## ## ## ## -->
    <@forms.renderField field="prop_journal_fieldQName" extension = { "control": {
        "template": "/ru/citeck/components/form/controls/select.ftl",
        "params": {
            "optionsUrl": "${url.context}/proxy/alfresco/search/search-attributes",
            "titleField": "shortName",
            "valueField": "fullName",
            "sortKey": "shortName",
            "responseType": "YAHOO.util.DataSource.TYPE_JSON",
            "responseSchema": "{ resultsList: 'attributes', fields: [ {key:'shortName'}, {key:'fullName'}] }"
        }
    } } />


    <#-- ## ## ## ## ## ## ## ## ##  RENDERING FIELD: PREDICATE ## ## ## ## ## ## ## ## ## -->
    <#assign criterionPredicat="${url.context}"+"/page/search/search-predicates?datatype=">


    <#-- INITIALIZING VARIABLES -->
    <script type="text/javascript">
        var formId="${formId}".substring(0,"${formId}".lastIndexOf("-form"));
        var fieldSelectID =  formId+"_prop_journal_fieldQName";
        var getSelectedQName = function () {
            var fieldSelect = Dom.get(fieldSelectID);
            if (fieldSelect) {
                return fieldSelect.options[fieldSelect.selectedIndex].innerHTML;
            } else return null;
        };

        // In first we must get the QName field value
        var fldSelectValue = null;
        <#assign keys = form.fields?keys>
        <#list keys as key>
            <#assign field = form.fields[key]>
            <#if field.id == "prop_journal_fieldQName">
                fldSelectValue = "${field.value}";
                <#assign fldSelectValue = field.value>
            </#if>
        </#list>
    </script>

    <#if form.mode != "view">
        <@forms.renderField field="prop_journal_predicate" extension = { "control": {
            "template": "/ru/citeck/components/form/controls/select-criterionPredicate.ftl",
            "params": {
                "qnameFull": "${fldSelectValue}",
                "titleField": "label",
                "valueField": "id",
                "sortKey": "id",
                "responseType": "YAHOO.util.DataSource.TYPE_JSON",
                "responseSchema": "{ resultsList: 'predicates', fields: [ {key:'id'}, {key:'label'}, {key:'needsValue'} ] }"
            }
        } }/>
    <#else>
        <@forms.renderField field="prop_journal_predicate"/>
    </#if>

    <#-- EVENT: ON CHANGE QNAME-SELECT -->
    <script type="text/javascript">//<![CDATA[
    (function() {
        var onChangeHandler = function(e, me) {
            var predicateSelectID = formId+"_prop_journal_predicate";
            var predicateSelectControl = Alfresco.util.ComponentManager.get(predicateSelectID);
            predicateSelectControl.options.optionsUrl="${criterionPredicat}";
            var fieldSelect = Dom.get(fieldSelectID);
            var fieldSelectControl = Alfresco.util.ComponentManager.get(fieldSelectID);
            if(predicateSelectControl && fieldSelectControl && fieldSelect) {
                var dataSourceType = new YAHOO.util.XHRDataSource("${url.context}/proxy/alfresco/search/search-attributes?attribute="
                        + getSelectedQName());
                dataSourceType.responseType = fieldSelectControl.options.responseType;
                dataSourceType.responseSchema = { resultsList: 'attributes', fields: [ {key:'datatype'}] };
                dataSourceType.sendRequest(fieldSelectControl.options.requestParam, {
                    success: function(request, response, payload) {
                        predicateSelectControl.options.requestParam = response.results[0]["datatype"];
                        var select = Dom.get(predicateSelectControl.id);
                        Dom.removeClass(select, "hidden");
                        Dom.get(predicateSelectControl.id+"-error").innerHTML = '';
                        var length = select.options.length;
                        for (i = 0; i < length; i++) {
                            select.options[i] = null;
                        }
                        predicateSelectControl.onReady();
                    },
                    scope: this
                });
            }
        };
        YAHOO.util.Event.addListener(fieldSelectID, "change", onChangeHandler, this);
    })();
    //]]>
    </script>

    <#-- ## ## ## ## ## ## ## ## ## RENDERING FIELD: CRITERION VALUE ## ## ## ## ## ## ## ## ## -->
    <@forms.renderField field="prop_journal_criterionValue"/>

</@>