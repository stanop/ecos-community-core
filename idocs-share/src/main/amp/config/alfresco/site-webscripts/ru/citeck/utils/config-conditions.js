
/**
 * Composite evaluator.
 * Contains other evaluators and evaluate nodes based on them.
 * Maintains composite logic: AND (default) or OR.
 * Example:
 * <any-node>
 *   <if type="evaluator-type" ...>
 *     evaluator staff
 *   </if>
 *   <if-not type="evaluator-type" ... />
 * </any-node>
 */
function CompositeEvaluator()
{
	// registry of evaluators:
	this._evaluators = {};
		
	// logic of evaluation:
	this._logic = "AND";
}

CompositeEvaluator.prototype = {
	
	/**
	 * Set different logic of evaluation
	 */
	setLogic: function(logic)
	{
		this._logic = logic;
	},
	
	/**
	 * Register evaluator
	 * @param type - type name of evaluator
	 * @param evaluator - evaluator object
	 *        evaluator should have method evaluate(node) 
	 */
	registerEvaluator: function (evaluator)
	{
		this._evaluators[evaluator.getType()] = evaluator;
	},
	
	/**
	 * Evaluate config node
	 * @param node - alfresco config node to evaluate
	 * @return true if all conditions are true on node
	 */
	evaluate: function (node)
	{
		var conditions = node.children;
		// if there are no conditions
		// the node is successful
		if(conditions == null) 
			return true;
			
		for(var ei = 0, eil = conditions.size(); ei < eil; ei++) 
		{
			var condition = conditions.get(ei),
				type = condition.attributes.type, 
				negate = false;
				
			// process condition name:
			if(condition.name == "if") {
				negate = false;
			} else if(condition.name == "if-not" || condition.name == "unless") {
				negate = true;
			} else {
				continue;
			}
			
			// process evaluator:
			var evaluator = this._evaluators[type];
			if(!evaluator) continue;
			var val = evaluator.evaluate(condition);
			
			// process negate:
			if(negate) val = !val;

			// process logic short evaluation:
			if(this._logic == "OR") {
				if(val) return true;
			} else {
				if(!val) return false;
			} 
		}
		
		// return standard value of logic:
		if(this._logic == "OR") {
			return false;
		} else {
			return true;
		}
	},
};

/**
 * Group evaluator
 * evaluates nodes like this:
 * <if type="group" value="ALFRESCO_ADMINISTRATORS" />
 */
function GroupEvaluator()
{
	// group names
	this._groups = null;
}

GroupEvaluator.prototype = {

	// groups lazy init
	_initGroups: function()
	{
		if(this._groups !== null) return;
		
		this._groups = [];
		
		var myconn = remote.connect("alfresco"),
			myres = myconn.get("/api/people/"+ encodeURIComponent(user.name) +"?groups=true");

		if (myres.status == 200) 
		{
			var groups = eval('(' + myres + ')').groups;
			for(var i in groups) 
			{
				this._groups.push(groups[i].itemName);
			}
		}
	},
	
	/**
	 * Gets type of evaluator
	 */
	getType: function ()
	{
		return "group";
	},
	
	/**
	 * Evaluate group
	 * @param condition - group condition node
	 */
	evaluate: function (condition)
	{
		// get group value
		var group = condition.attributes.value;
		this._initGroups();
		return this._groups.indexOf(group) != -1 || this._groups.indexOf("GROUP_"+group) != -1;
	},
	
};

/**
 * User evaluator
 * evaluates nodes like this:
 * <if type="user" value="admin" />
 */
function UserEvaluator()
{
}

UserEvaluator.prototype = {
	
	/**
	 * Gets type of evaluator
	 */
	getType: function ()
	{
		return "user";
	},
	
	/**
	 * Evaluate user
	 * @param condition - user condition node
	 */
	evaluate: function (condition)
	{
		// get user value
		var username = condition.attributes.value;
		return user.name == username;
	},
	
};

/**
 * Type evaluator
 * evaluates nodes like this:
 * <if type="type" value="cm:content" />
 * @param dao - DAO object to access node information
 *        dao should have getTypes() method
 */
function TypeEvaluator(dao)
{
	// save dao object:
	this._dao = dao;
}

TypeEvaluator.prototype = {
		
	/**
	 * Gets type of evaluator
	 */
	getType: function ()
	{
		return "type";
	},
	
	/**
	 * Evaluate type
	 * @param condition - type condition node
	 */
	evaluate: function (condition)
	{
		var type = condition.attributes.value;
		return this._dao.getTypes().indexOf(type) != -1;
	}

};


/**
 * Aspect evaluator
 * evaluates nodes like this:
 * <if type="aspect" value="cm:generalclassifiable" />
 * @param dao - DAO object to access node information
 *        dao should have getAspects() method
 */
function AspectEvaluator(dao)
{
	// save dao object:
	this._dao = dao;
}

AspectEvaluator.prototype = {
		
	/**
	 * Gets type of evaluator
	 */
	getType: function ()
	{
		return "aspect";
	},
	
	/**
	 * Evaluate type
	 * @param condition - type condition node
	 */
	evaluate: function (condition)
	{
		var aspect = condition.attributes.value;
		return this._dao.getAspects().indexOf(aspect) != -1;
	}

};

/**
 * Property evaluator
 * evaluates nodes like this:
 * <if type="property" name="dms:subject" value="on-invoice" />
 * @param dao - DAO object to access node information
 *        dao should have getProperties() method
 */
function PropertyEvaluator(dao)
{
	// save dao object:
	this._dao = dao;
}

PropertyEvaluator.prototype = {

	/**
	 * Gets type of evaluator
	 */
	getType: function ()
	{
		return "property";
	},

	/**
	 * Evaluate type
	 * @param condition - type condition node
	 */
	evaluate: function (condition)
	{
		var name = condition.attributes.name;
		var value = condition.attributes.value;
//		throw "I am here "+this._dao.getProperties();
		return (this._dao.getProperties()[name] == value);
	}

};

/**
 * Default node DAO
 * @param nodeRef - nodeRef of object to access
 */
function NodeDAO(nodeRef)
{
	// check nodeRef:
	if(!nodeRef || !nodeRef.match(/^\w+\:\/\/\w+\/[\d\w-]+$/))
	{
		throw "Invalid nodeRef: " + nodeRef;
	}

	// save nodeRef
	this._nodeRef = nodeRef;

	// document info cache
	this._doc = null;
	
	// types info cache
	this._types = null;
	
	// aspects info cache
	this._aspects = null;

	// properties info cache
	this._properties = null;
}


NodeDAO.prototype = {
	
	// lazy init of _doc
	_initDoc: function ()
	{
		if(this._doc !== null) return;
		this._doc = {};
		var nodeRef = this._nodeRef;
		var myconn = remote.connect("alfresco"),
			myres = myconn.get("/slingshot/doclib2/node/" + nodeRef.replace(":/",""));

		if (myres.status == 200)
		{
			this._doc = eval('(' + myres + ')');
		}
	},
	
	// lazy init of _types
	_initTypes: function ()
	{
		if(this._types !== null) return;
		this._types = [];
		this._initDoc();
		var type = this._doc.item ? this._doc.item.node.type : null;
		
		while(type) {
			this._types.push(type);
			var myconn = remote.connect("alfresco"),
				myres = myconn.get("/api/classes/" + type.replace(":","_"));

			if (myres.status == 200)
			{
				type = eval('(' + myres + ')').parent.name;
			}
			else
			{
				type = null;
			}
		}
	},
	
	// lazy init of _aspects
	_initAspects: function ()
	{
		if(this._aspects !== null) return;
		this._aspects = [];
		this._initDoc();
		if(this._doc.item)
		{
			this._aspects = this._doc.item.node.aspects;
		}
	},

	// lazy init of _properties
	_initProperties: function ()
	{
		if(this._properties !== null) return;
		this._properties = [];
		this._initDoc();
		if(this._doc.item)
		{
			this._properties = this._doc.item.node.properties;
		}
	},
	
	/**
	 * Get array of type names of node, including concrete type and all parent types
	 */
	getTypes: function ()
	{
		this._initTypes();
		return this._types;
	},
	
	/**
	 * Get aspect names of node
	 */
	getAspects: function ()
	{
		this._initAspects();
		return this._aspects;
	},

	/**
	 * Get array of {property_name, property_value}
	 */
	getProperties: function ()
	{
		this._initProperties();
//		throw "I am here "+this._properties;
		return this._properties;
	},

};
