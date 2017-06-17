{
    "status": "${mirror.properties[shortQName("bpm:status")]}"<#if mirror.properties[shortQName("bpm:completionDate")]??>,
    "completionDate": "${(mirror.properties[shortQName("bpm:completionDate")]?datetime)?iso_local}"
    </#if>
}