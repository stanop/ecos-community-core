<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<#if page?? && (page.url.args.packageItems!"") != "">
	<#assign packageItemsParams = {
		"allowAddAction": false,
		"allowRemoveAllAction": false,
		"allowRemoveAction": false,
		"allowViewMoreAction": false
	} />
<#else/>
	<#assign packageItemsParams = {} />
</#if>


<#if allowAddAction??>
	<#assign packageItemsParams = packageItemsParams + {
		"allowAddAction": allowAddAction
	} />
</#if>
<#if allowRemoveAllAction??>
	<#assign packageItemsParams = packageItemsParams + {
		"allowRemoveAllAction": allowRemoveAllAction
	} />
</#if>
<#if allowRemoveAction??>
	<#assign packageItemsParams = packageItemsParams + {
		"allowRemoveAction": allowRemoveAction
	} />
</#if>
<#if allowViewMoreAction??>
	<#assign packageItemsParams = packageItemsParams + {
		"allowViewMoreAction": allowViewMoreAction
	} />
</#if>
<#if startLocation??>
    <#assign packageItemsParams = packageItemsParams + {
		"startLocation": startLocation
	} />
</#if>

<div class="set">
	<div class="set-title">${msg("workflow.set.items")}</div>
	<@forms.renderField field="assoc_packageItems" extension = {
		"control": {
			"template": "/ru/citeck/components/form/controls/workflow/packageitems.ftl",
			"params": packageItemsParams
		}
	}/>
</div>
