
<script type="text/javascript">//<![CDATA[
(function () {

    new Citeck.JSVariableField("${fieldHtmlId}").setOptions({
    <#if field.control.params.variableName??>
        variableName: "${field.control.params.variableName!""}",
    </#if>
        formMode: "${form.mode}"
    });

})();
//]]></script>


<div class="form-field">
    <input id="${fieldHtmlId}" type="hidden" name="${field.name}" />
</div>