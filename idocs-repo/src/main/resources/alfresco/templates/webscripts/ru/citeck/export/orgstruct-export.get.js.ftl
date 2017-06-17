<#global processedGroups = [] />

<#macro renderOrgstruct group>
<#if processedGroups?seq_contains(group.shortName)>
	<#return/>
<#else>
	<#assign processedGroups = processedGroups + [group.shortName] />
</#if>
if(groups.getGroup("${group.shortName}") == null) {
    groups.createRootGroup("${group.shortName}", "${group.displayName}");
}
    <#assign groupNode = group.groupNode />
	<#if groupNode.hasAspect("org:branch")>
orgstruct.createTypedGroup("branch", "${groupNode.properties["org:branchType"]}", "${group.shortName}");
    </#if>
    <#if groupNode.hasAspect("org:role")>
orgstruct.createTypedGroup("role", "${groupNode.properties["org:roleType"]}", "${group.shortName}");
    </#if>
    <#list group.childGroups as child>
        <@renderOrgstruct child />
groups.getGroup("${group.shortName}").addAuthority("${child.fullName}");
    </#list>
    <#list group.childUsers as child>
if(people.getPerson("${child.userName}") == null) {
    people.createPerson("${child.userName}", "${child.person.properties.firstName!}", "${child.person.properties.lastName!}", "${child.person.properties.email!}", "${args.password!child.userName}", true);
}
groups.getGroup("${group.shortName}").addAuthority("${child.userName}");
    </#list>
</#macro>

<#compress>
<#list branchTypes as type>
type = orgmeta.createSubType("branch", "${type.name}");
<#if type.properties["cm:title"]??>
    type.properties["cm:title"] = "${type.properties["cm:title"]}";
    type.save();
</#if>
</#list>
<#list roleTypes as type>
type = orgmeta.createSubType("role", "${type.name}");
<#if type.properties["cm:title"]??>
    type.properties["cm:title"] = "${type.properties["cm:title"]}";
</#if>
    type.properties["org:roleIsManager"] = ${type.properties["org:roleIsManager"]?string};
    type.save();
</#list>

<@renderOrgstruct orgstructRoot />
</#compress>