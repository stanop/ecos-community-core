/**
 * It copies all args (arguments) to model.params object.
 */
(function() {
	function cloneArgs(args) {
		var result = {};
		for (var arg in args)
			result[arg] = args[arg];
		return result;
	}

	var params = cloneArgs(args);
	model.params = params;
})();
