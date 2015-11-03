<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<#if form.mode == "create" && (page.url.args.outcome!) != "" >
<script type="text/javascript">//<![CDATA[


        YAHOO.Bubbling.on("objectFinderReady", function(layer, args) {
        var control = args[1].eventGroup;
        if(control.id != "${args.htmlid}_assoc_letters_outcome-cntrl" ) {
            return;
        }
        control.selectItems("${page.url.args.outcome}");
		$("#" + "${args.htmlid}_assoc_letters_outcome-cntrl-itemGroupActions").hide();
        });

//]]></script>
</#if>

<@forms.setMandatoryFields
    fieldNames = [
    "assoc_letters_nomenclature",
    "prop_tk_kind",
    "assoc_letters_reporterOrganization",
    "prop_letters_letterWriter",
    "assoc_letters_subdivision",
    "prop_letters_receiver",
    "prop_cm_content"
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

<div class="${twoColumnClass}">
    <div class="yui-u first">
        <@forms.renderField field="assoc_letters_outcome" extension = {
			"control": {
				"template": "/ru/citeck/components/form/controls/association_search.ftl",
				"params": {
					"flatButtonMode": "true",
					"searchWholeRepo": "true",
					"showTargetLink": "true"
				}
			}
		} />
    </div>
    <div class="yui-u">
        <@forms.renderField field="assoc_letters_nomenclature" extension = {
			"label": "Дело",
			"control": {
				"template": "/ru/citeck/components/form/controls/association_search.ftl",
				"params": {
					"flatButtonMode": "true",
					"searchWholeRepo": "true",
					"showTargetLink": "true"
				}
			}
		} />
    </div>
</div>
<div class="${twoColumnClass}">
    <div class="yui-u first">
        <@forms.renderField field="prop_tk_kind" extension = {
			"label" : "Вид документа",
			"control" : {
				"template" : "/ru/citeck/components/form/controls/select.ftl",
				"params": {
					"optionsUrl": "/share/proxy/alfresco/citeck/subcategories?nodeRef=workspace://SpacesStore/letter-doctype-income",
					"titleField": "name",
					"valueField": "nodeRef",
					"responseType": "YAHOO.util.DataSource.TYPE_JSON",
					"responseSchema": "{ resultsList: 'nodes', fields: [ {key:'nodeRef'}, {key:'name'} ] }"
				}
			} 
		} />
    </div>
    <div class="yui-u">
        <#--<@forms.renderField field="prop_letters_outcomeDate" />-->
    </div>
</div>
<div class="${twoColumnClass}">
    <div class="yui-u first">
        <@forms.renderField field="assoc_letters_reporterOrganization" extension = {
			"control": {
				"template": "/ru/citeck/components/form/controls/association_search.ftl",
				"params": {
					"flatButtonMode": "true",
					"searchWholeRepo": "true",
					"showTargetLink": "true"
				}
			}
		} />
    </div>
    <div class="yui-u">
        <@forms.renderField field="prop_letters_letterWriter" />
    </div>
</div>
<div class="${twoColumnClass}">
    <div class="yui-u first">
        <@forms.renderField field="assoc_letters_subdivision" extension = {
			"label" : "Подразделение",
			"control": {
				"template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
				"params": {
					"flatButtonMode": "true",
					"selectable": "GROUP-branch",
					"searchWholeRepo": "true"
				}
			}
		} />
    </div>
    <div class="yui-u">
        <@forms.renderField field="prop_letters_receiver" />
    </div>
</div>
<div class="${twoColumnClass}">
    <div class="yui-u first">
        <@forms.renderField field="prop_idocs_note" />
    </div>
    <div class="yui-u">
        <@forms.renderField field="prop_idocs_summary" />
    </div>
</div>
<div class="${twoColumnClass}">
    <div class="yui-u first">
        <@forms.renderField field="prop_letters_deliveryMethod" />
    </div>
    <div class="yui-u">
        <@forms.renderField field="prop_idocs_pagesNumber" />
    </div>
</div>
<div class="${twoColumnClass}">
    <div class="yui-u first">
        <@forms.renderField field="prop_idocs_appendixPagesNumber" />
    </div>
    <div class="yui-u">
        <@forms.renderField field="prop_letters_originalLocation" />
    </div>
</div>
<div class="${twoColumnClass}">
    <div class="yui-u first">
        <@forms.renderField field="prop_letters_copiesCount" />
    </div>
    <div class="yui-u">
        <@forms.renderField field="prop_cm_content" extension = {
			"control": {
				"template": "/ru/citeck/components/form/controls/fileUpload.ftl",
				"params": {
				}
			}
		} 
/>
    </div>
</div>
</@>
