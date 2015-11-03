(function() {
var elementType = args.type;
if(!elementType) {
    status.setCode(status.STATUS_BAD_REQUEST, "Argument type has not been provided.");
    return;
}

var query = 'TYPE:"journal:criterion" AND @journal\\:fieldQName:"type" AND @journal\\:predicate:"type-equals" AND @journal\\:criterionValue:"'+utils.longQName(elementType)+'"';
var criteria = search.luceneSearch(query);
var journals = [];
for(var i in criteria) {
	var criterion = criteria[i];
	if(criterion.parent.isSubType('journal:journal')) {
		journals.push(criterion.parent);
	}
}

model.journals = journals;
})();