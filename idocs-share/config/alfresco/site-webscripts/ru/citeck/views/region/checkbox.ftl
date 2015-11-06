<#-- many checkboxes with label -->
<!-- ko if: options -->
  <!-- ko foreach: options -->
    <span class="checkbox-option" style="margin-right: 15px; white-space: nowrap;">
        <#-- select many options -->
        <!-- ko if: $parent.multiple -->
          <input type="checkbox" data-bind="checked: ko.computed({ 
            read: function() {
              if ($parent.value())
                return $parent.value().indexOf($data) != -1;
            }, 
            write: function(newValue) {
              var selectedOptions = $parent.value() || [];
              if (newValue) {
                selectedOptions.push($data);
              } else {
                var index = selectedOptions.indexOf($data);
                selectedOptions.splice(index, 1);
              }
              $parent.value(selectedOptions);
            } 
          })" />
        <!-- /ko -->

        <#-- select only one option -->
        <!-- ko ifnot: $parent.multiple -->
          <input type="checkbox" data-bind="checked: ko.computed({ 
            read: function() { return $parent.value() == $data; }, 
            write: function(newValue) { newValue ? $parent.value($data) : $parent.value(null) } 
          })" />
        <!-- /ko -->

        <!-- ko text: $parent.getValueTitle($data) --><!-- /ko -->
    </span>
  <!-- /ko -->
<!-- /ko -->

<#-- single checkbox without label -->
<!-- ko ifnot: options -->
    <input id="${fieldId}" type="checkbox" data-bind="checked: value, disable: protected" />
<!-- /ko -->