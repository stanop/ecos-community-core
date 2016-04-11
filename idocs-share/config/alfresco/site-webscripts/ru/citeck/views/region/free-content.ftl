<#assign params = viewScope.region.params!{} />

<#if params.html??>
  ${params.html?string}
<#elseif params.function??>
  <!-- ko component: { name: "free-content", params: {
    func: ko.computed(function() { ${params.function} })
  }} --><!-- /ko -->
<#else>
  <span style="color: red;">
    You should specified parameter "html" or "function"
  </span>
</#if>

<#if params.css??>
  <style type="text/css">
    ${params.css?string}
  </style>
</#if>