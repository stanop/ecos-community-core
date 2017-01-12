<#include "/org/alfresco/include/alfresco-template.ftl" />

<@templateHeader>
<!-- CSS and JavaScript files are linked in here -->
</@>

<@templateBody>
<div id="alf-hd">
    <#include "/ru/citeck/include/header.ftl" />
</div>

<div id="bd">
	<div class="share-form">
		<@region id="data" scope="template" />
		<@region id="edit-metadata" scope="template" />
	</div>
</div>
</@>

<@templateFooter>
<div id="alf-ft">
	<@region id="footer" scope="global" protected=true />
</div>
</@>