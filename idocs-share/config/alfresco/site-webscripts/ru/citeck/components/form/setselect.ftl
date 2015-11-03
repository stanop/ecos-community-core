<#assign set_id = args.htmlid + "_" + set.id />


<#-- get caption field or generate default if not present -->

<#if set.caption_field?? && form.fields[set.caption_field]??>
	<#assign caption = form.fields[set.caption_field] />
<#else>
	<#assign caption_name = set.id + "-caption" />
	<#assign caption = {
		"id": caption_name,
		"name": caption_name
	} />
</#if>
<#assign caption_id = args.htmlid + "_" + caption.id + "-container" />


<#-- generate options for caption -->

<#assign caption_options = "" />
<#assign optionSeparator = "#option#" />
<#assign labelSeparator = "#label#" />
<#list set.children as item>
	<#assign value = item.id />
	<#assign label = item.label />
	<#if caption_options?length &gt; 0>
		<#assign caption_options = caption_options + optionSeparator />
	</#if>
	<#assign caption_options = caption_options + value + labelSeparator + label />
</#list>


<#-- javascript function to show specified set -->

<script>
function getActiveSet() {
	var input = YAHOO.util.Selector.query("input:checked", "${caption_id}", true);
	return input.value;
}

function updateSets() {
	var active_set = getActiveSet();
	active_set = "${args.htmlid}_wrap_" + active_set;
	var superset = document.getElementById("${set_id}-body");
	YAHOO.util.Dom.setStyle(superset.children, "display", "none");
	YAHOO.util.Dom.setStyle(active_set, "display", "");
	YAHOO.Bubbling.fire("mandatoryControlValueUpdated", active_set);
}
</script>


<#-- render caption -->

<@formLib.renderField field = caption + {
	"control": {
		"template": "/ru/citeck/components/form/controls/selectbar.ftl",
		"params": {
			"styleClass": "setselector",
			"optionSeparator": optionSeparator,
			"labelSeparator": labelSeparator,
			"options": caption_options
		} + set.caption_params!{}
	}
} />

<#-- render subsets (subitems?) -->

<div id="${set_id + "-body"}">

<#list set.children as item>
	<div id="${args.htmlid + '_wrap_' + item.id}">
		<#if item.kind == "set">
			<@formLib.renderSet set = item + { "appearance": "" } />
		<#else>
			<@formLib.renderField field = form.fields[item.id] />
		</#if>
	</div>
</#list>

</div>

<#-- show selected subsets (subitems) -->

<script>
YAHOO.util.Event.onAvailable("${set_id + "-body"}", updateSets, null);
YAHOO.util.Event.onAvailable("${caption_id}", function() {
	YAHOO.util.Event.on(YAHOO.util.Selector.query("input", "${caption_id}"), "click", updateSets);
}, null);
</script>
