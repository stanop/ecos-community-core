<#if form.mode == "edit" && ((form.data['prop_bpm_status']?? && form.data['prop_bpm_status'] != 'Completed') || form.data['prop_bpm_status']?? == false)>

<script type="text/javascript">//<![CDATA[
(function()
{
    var transitions = "${field.control.params.options?js_string}";
    var transitionPairs = transitions.split("#alf#");
    transitions="";
    for (var i = 0, ii = transitionPairs.length; i < ii; i++)
    {
        var transitionInfo = transitionPairs[i].split("|");
        transitionInfo[1] = Alfresco.util.message(transitionInfo[1]);
        transitions += transitionInfo[0]+"|"+transitionInfo[1]+(i<ii-1?"#alf#":"");
    }

    new Alfresco.ActivitiTransitions("${fieldHtmlId}").setOptions(
            {
                currentValue: transitions,
                hiddenFieldName: "${field.name}"
            }).setMessages(
               ${messages}
            );
})();
//]]></script>

<div class="form-field suggested-actions" id="${fieldHtmlId}">
   <div id="${fieldHtmlId}-buttons">
   </div>
</div>
</#if>