<?xml version="1.0" encoding="UTF-8"?>
<response xmlns="http://www.citeck.ru/ecos/cases/case-documents">
    <@renderContainer container />
    <containerKinds>
        <#list containerKinds?values as type>
        <containerKind>
            <nodeRef>${type.node.nodeRef}</nodeRef>
            <name>${type.node.name}</name>
            <documentKinds>
                <#list type.documentKinds as kind>
                <documentKind>
                    <nodeRef>${kind.kind.nodeRef}</nodeRef>
                    <mandatory>${kind.mandatory?string}</mandatory>
                    <multiple>${kind.multiple?string}</multiple>
                    <#if kind.containerType??>
                        <#if kind.containerType?is_string>
                    <containerType>${kind.containerType}</containerType>
                        <#elseif kind.containerType?is_sequence>
                            <#list kind.containerType as type>
                    <containerType>${type}</containerType>
                            </#list>
                        </#if>
                    </#if>
                    <#if kind.loanType??>
                        <#if kind.loanType?is_string>
                    <loanType>${kind.loanType}</loanType>
                        <#elseif kind.loanType?is_sequence>
                            <#list kind.loanType as type>
                    <loanType>${type}</loanType>
                            </#list>
                        </#if>
                    </#if>
                </documentKind>
                </#list>
            </documentKinds>
        </containerKind>
        </#list>
    </containerKinds>
    <documentTypes>
        <#list documentTypes?values as type>
        <documentType>
            <nodeRef>${type.nodeRef}</nodeRef>
            <name>${type.name}</name>
        </documentType>
        </#list>
    </documentTypes>
    <documentKinds>
        <#list documentKinds?values as record>
        <documentKind>
            <nodeRef>${record.kind.nodeRef}</nodeRef>
            <name>${record.kind.name}</name>
            <type>${record.type.nodeRef}</type>
        </documentKind>
        </#list>
    </documentKinds>
    <stages>
        <#list stages as stage>
        <stage>${stage}</stage>
        </#list>
    </stages>
</response>

<#macro renderDocument document>
<document>
    <nodeRef>${document.nodeRef}</nodeRef>
    <name>${document.name}</name>
    <#if document.properties['tk:type']??><type>${document.properties['tk:type'].nodeRef}</type></#if>
    <#if document.properties['tk:kind']??><kind>${document.properties['tk:kind'].nodeRef}</kind></#if>
    <#if document.properties.modified??><uploaded>${xmldate(document.properties.modified)}"</uploaded></#if>
    <#if document.properties.modifier??><uploader>
        <#assign uploader = people[document.properties.modifier] />
        <userName>${uploader.properties['cm:userName']}</userName>
        <firstName>${uploader.properties['cm:firstName']!}</firstName>
        <lastName>${uploader.properties['cm:lastName']!}</lastName>
    </uploader></#if>
</document>
</#macro>

<#macro renderContainer container>
<container>
    <nodeRef>${container.node.nodeRef}</nodeRef>
    <name><#if container.node.hasAspect('icase:subcase')>${container.node.assocs['icase:subcaseElement'][0].name}<#else>${container.node.name}</#if></name>
    <kind>${container.kind.nodeRef}</kind>
    <containers>
    <#list container.containers as item>
        <@renderContainer item />
    </#list>
    </containers>
    <documents>
    <#list container.documents as item>
        <@renderDocument item />
    </#list>
    </documents>
</container>
</#macro>
