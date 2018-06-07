<@outputCSS/>
<@outputJavaScript/>
<#assign chromelessParam = (args.chromeless!"false") />
<@region id="${args.regionId}" scope="page" chromeless=chromelessParam source="card-details" />