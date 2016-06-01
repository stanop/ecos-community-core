<#assign params = viewScope.region.params>

<#-- many checkboxes with label -->
<div class="alignment-${params.alignment!'horizontal'}">
  <!-- ko ifnot: datatype() == 'd:boolean' -->
    <!-- ko component: { name: "checkbox-radio", params: {
      groupName: name,
      options: options,
      value: value,
      multiple: multiple,
      optionText: function(object) { return getValueTitle(object); }
    }} --><!-- /ko -->
  <!-- /ko -->
</div>

<#-- single checkbox without label -->
<!-- ko if: datatype() == 'd:boolean' -->
    <input id="${fieldId}" type="checkbox" data-bind="checked: value, disable: protected" />
<!-- /ko -->