<div style="padding-left: 10px" class="criterion-value-field-view">
    <!-- ko ifnot: empty -->
        <!-- ko foreach: multipleValues -->
            <span class="value-item">
                <span class="value-item-text" data-bind="text: $parent.getValueTitle($data), attr: { title: $parent.getValueDescription($data) }"></span>
            </span>
        <!-- /ko -->
    <!-- /ko -->
    <!-- ko if: empty -->
        <span class="value-item">
            <span class="value-item-text">(${msg("label.no")})</span>
        </span>
    <!-- /ko -->
</div>