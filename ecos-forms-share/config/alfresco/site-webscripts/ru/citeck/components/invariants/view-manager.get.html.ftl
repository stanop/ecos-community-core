<@markup id="js">
	<@script type="text/javascript" src="${url.context}/res/citeck/components/invariants/view-manager.js" group="node-view"/>
	<@inlineScript group="node-view">
		new Citeck.invariants.NodeViewManager("${args.runtimeKey}").setOptions({
			<#if args.onsubmit??>onsubmit: "${args.onsubmit}",</#if>
			<#if args.oncancel??>oncancel: "${args.oncancel}",</#if>
		});
	</@inlineScript>
</@>
