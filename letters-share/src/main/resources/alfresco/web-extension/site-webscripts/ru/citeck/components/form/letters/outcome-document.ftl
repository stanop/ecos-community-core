<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<#if form.mode == "create" && (page.url.args.income!) != "" >
<script type="text/javascript">//<![CDATA[


        YAHOO.Bubbling.on("objectFinderReady", function(layer, args) {
        var control = args[1].eventGroup;
        if(control.id != "${args.htmlid}_assoc_letters_income-cntrl" ) {
            return;
        }
        control.selectItems("${page.url.args.income}");
        });

//]]></script>
</#if>

<@forms.setMandatoryFields
    fieldNames = [
    "assoc_letters_addressee",
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
        <@forms.renderField field="assoc_letters_income" extension = {
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
        <@forms.renderField field="assoc_idocs_legalEntity" extension = {
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
        <@forms.renderField field="assoc_idocs_performer" extension = {
			"control": {
				"template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
				"params": {
					"flatButtonMode": "true"
				}
			}
		} />
    </div>
    <div class="yui-u">
        <@forms.renderField field="prop_letters_deliveryMethod" />
    </div>
</div>
<div class="${twoColumnClass}">
    <div class="yui-u first">
        <@forms.renderField field="assoc_idocs_signatory" extension = {
			"control": {
				"template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
				"params": {
					"flatButtonMode": "true"
				}
			}
		} />
    </div>
    <div class="yui-u">
        <@forms.renderField field="assoc_letters_addressee" extension = {
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
        <@forms.renderField field="prop_idocs_summary" />
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
        <@forms.renderField field="prop_idocs_note" />
    </div>
</div>
<div class="${twoColumnClass}">
    <div class="yui-u first">
        <@forms.renderField field="prop_cm_content" extension = {
			"control": {
				"template": "/ru/citeck/components/form/controls/fileUpload.ftl",
				"params": {
				}
			}
		} 
/>
    </div>
    <div class="yui-u">
    </div>
</div>
<#if form.mode == "create" && (page.url.args.income!) != "" >
	<script type="text/javascript">//<![CDATA[


		Alfresco.util.Ajax.jsonGet({
			url: Alfresco.constants.PROXY_URI + "citeck/node?nodeRef="+"${page.url.args.income}",
			successCallback: {
				fn: function(response) {
					var incomeNode = response.json;
					
					//Юр лицо
					var legalEntityControl = Alfresco.util.ComponentManager.get("${args.htmlid}_assoc_idocs_legalEntity-cntrl");
					if(legalEntityControl) {
						legalEntityControl.selectItems((incomeNode.assocs['idocs:legalEntity']||[]).join(','));

					}
					
					//корреспондент					
					var addresseeControl = Alfresco.util.ComponentManager.get("${args.htmlid}_assoc_letters_addressee-cntrl");
					if(addresseeControl) {
						addresseeControl.selectItems((incomeNode.assocs['letters:reporterOrganization']||[]).join(','));
					}
					
				}
			}
		});

	//]]></script>
</#if>
</@>
