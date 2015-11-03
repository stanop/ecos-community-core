(function() {
	model.nodes = [];
	model.query = '';
	model.skipCount = 0;
	var meetQuestion = args.meetQuestion;

	if (args.caseNodeRef) {
		var caseNode = search.findNode(args.caseNodeRef);
		if (caseNode) {
			var childAgendas = caseNode.childAssocs['meet:childAgenda'];
			var childAgenda = (childAgendas && childAgendas.length) ? childAgendas[0] : null;
			if (childAgenda)
			{
				var childQuestions = childAgenda.childAssocs['meet:childQuestions'] || [];
				for(var i=0; i<childQuestions.length; i++)
				{
					if(!meetQuestion || (meetQuestion && meetQuestion==childQuestions[i].properties["meet:question"]))
						model.nodes.push(childQuestions[i]);
				}
			}
		}
	}
	model.maxItems = model.nodes.length;
})();
