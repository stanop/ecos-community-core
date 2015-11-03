<#import "journals.lib.ftl" as journals />
<#escape x as jsonUtils.encodeJSONString(x)>{
    "journalType": "${journalType}",
    "settings": <@journals.renderSettingsList journalsSettings />
}
</#escape>
