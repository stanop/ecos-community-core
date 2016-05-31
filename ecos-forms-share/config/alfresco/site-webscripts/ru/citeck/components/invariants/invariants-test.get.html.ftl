<#import "/ru/citeck/views/views.lib.ftl" as views />

<@views.renderViewContainer view args.htmlid />

<script type="text/javascript">//<[!CDATA[
<#escape x as x?js_string>
new Citeck.invariants.InvariantsRuntime("${args.htmlid}-form").setOptions({
	model: {
		node: {
			nodeRef: 'test',
			type: "${type}",
		},
		invariants: {
			key: 'test',
			defaultModel: <@views.renderModel defaultModel />,
			invariants: <@views.renderInvariants invariants />
		}
	}
});
</#escape>
//]]></script>
