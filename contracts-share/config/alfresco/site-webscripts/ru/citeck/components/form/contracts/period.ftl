<#include "/ru/citeck/templates/multipart-form.ftl"/>

<#if form.mode == "create">
<script type="text/javascript">// <![CDATA[
(function(){
    window.onbeforeunload = function(event) {
        var source = event.srcElement || event.target;
        var activeElement = source.activeElement || source.document.activeElement;
        if (activeElement.localName == "button") {
            return;
        } else {
            return "Все несохраненные данные будут потеряны.";
        }
    };
})();
//]]></script>
</#if>