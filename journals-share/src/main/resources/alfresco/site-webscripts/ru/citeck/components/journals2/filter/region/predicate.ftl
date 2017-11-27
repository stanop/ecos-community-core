<div class="criterion-predicate">
    <!-- ko if: resolve('field.datatype.predicates.length', 0) == 0 -->
    <input type="hidden" data-bind="attr: { name: 'predicate_' + id() }, value: predicate().id()" />
    <!-- /ko -->
    <!-- ko if: resolve('field.datatype.predicates.length', 0) > 0 -->
    <select data-bind="attr: { name: 'predicate_' + id() },
							   value: predicate,
							   options: resolve('field.datatype.predicates'),
							   optionsText: 'label'
	"></select>
    <!-- /ko -->
</div>