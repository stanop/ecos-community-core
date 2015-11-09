<#-- many checkboxes with label -->
<!-- ko if: options -->
  <!-- ko component: { name: "checkbox-radio", params: {
    options: options,
    value: value,
    multiple: multiple,
    optionText: function(object) { return getValueTitle(object); }
  }} --><!-- /ko -->
<!-- /ko -->

<#-- single checkbox without label -->
<!-- ko ifnot: options -->
    <input id="${fieldId}" type="checkbox" data-bind="checked: value, disable: protected" />
<!-- /ko -->