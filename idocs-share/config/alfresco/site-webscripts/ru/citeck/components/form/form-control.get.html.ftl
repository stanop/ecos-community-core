<#import "/org/alfresco/components/form/form.lib.ftl" as formLib />

<#macro renderControl>

<#if !(form??)>
	<div class="error">form is not found: check 'itemKind' and 'itemId' arguments</div>
	<#return />
</#if>

<#if !(args.field??)>
	<div class="error">field is not specified: check 'field' argument</div>
	<#return />
</#if>

<#-- find the field -->
<#list form.fields?values as field1>
	<#if field1.configName == args.field>
		<#assign field = field1 />
		<#break />
	</#if>
</#list>

<#if !(field??)>
	<div class="error">field '${args.field}' is not found</div>
	<#return />
</#if>

<#if args.name??>
	<#assign name = args.name />
<#else />
	<#assign name = field.name />
</#if>

<#if args.value??>
	<#assign value = args.value />
<#else />
	<#assign value = field.value!"" />
</#if>

<#if args.disabled??>
	<#assign disabled = args.disabled == "true" />
<#else>
	<#assign disabled = field.disabled />
</#if>

<@formLib.renderField field = field + {
	"name": name,
	"value": value,
	"disabled": false
} />

</#macro>

<@renderControl />