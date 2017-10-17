<#import "/ru/citeck/views/views.lib.ftl" as views />

<@standalone>
	<@markup id="css" >
		<@views.nodeViewStyles />
	</@>

	<@markup id="js">
		<@views.nodeViewScripts />
	</@>

	<@markup id="widgets">
		<@views.nodeViewWidget nodeRef=viewNodeRef!args.nodeRef type=args.type />
	</@>

	<@markup id="html">
		<@views.renderViewContainer view args.htmlid />
	</@>
</@>