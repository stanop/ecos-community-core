<@outputCSS/>
<@outputJavaScript/>
<#assign source = args.source!args.pageid!"" />
<#assign scope = args.scope!"global" />
<#if source?has_content>
 <@region id=args.regionId scope=scope chromeless=(args.chromeless!"false") source=source />
<#else>
 <@region id=args.regionId scope=scope chromeless=(args.chromeless!"false") />
</#if>
<@relocateJavaScript/>
