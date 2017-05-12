/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
define(['lib/knockout'], function(ko) {

	// locale for all yui requests
	var customLocale = YAHOO.util.Cookie.get("alf_share_locale");
	if (customLocale) YAHOO.util.Connect.initHeader("Accept-Language", customLocale, true);

	var logger = Alfresco.logger,
		fail = function(message, silent) {
			logger.error(message);
			if(userPrompts && !silent) {
			    Alfresco.util.PopupManager.displayPrompt({
			        title: Alfresco.util.message("message.failure"),
			        text: message
			    });
			} else {
			    throw message;
			}
		},
		assert = function(condition, message, silent) {
			if(!condition) fail(message, silent);
		},
		userPrompts = false,
		simpleLoadCache = {},
		koclasses = {};

	koclasses.ViewModel = function(model) {
		return this;
	};

	koclasses.ViewModel.prototype = {
		constructor: koclasses.ViewModel,
		toString: function() {
			var ViewModelClass = this.thisclass,
				keyProperty = ViewModelClass.key();
			if(typeof keyProperty != "undefined") {
				return ViewModelClass + "[" + keyProperty + "=" + this[keyProperty]._value() + "]";
			} else {
				return ViewModelClass.toString();
			}
		},
		model: function() {
			if(arguments.length == 0) {
				return this.getModel();
			} else {
				return this.setModel.apply(this, arguments);
			}
		},
		getModel: function() {
			return {};
		},
		setModel: function() {
			// abstract method
		},
		clone: function() {
			return new this.thisclass(this);
		},
		resolve: function() {
			var paths, 
				result = null, 
				defaultValue = null;
			switch(arguments.length) {
			case 0:
				fail("At least one path should be specified");
			case 1:
				paths = arguments;
				break;
			default:
				paths = _.initial(arguments, 1);
				defaultValue = _.last(arguments);
			}
			result = _.reduce(paths, function(result, path) {
				if(result != null) return result;
				if(typeof path == "string") {
					path = path.split(/\./);
				} else if(path == null) {
					path = [];
				}
				logger.debug("Resolving " + this + " : '" + path.join('.') + "'");
				var result = _.reduce(path, function(viewModel, key) {
				    if(viewModel == null) return null;
				    return typeof viewModel[key] == "function" ? viewModel[key]() : viewModel[key];
				}, this);
				logger.debug("Resolved " + this + " : '" + path.join('.') + "' to " + (result || "null"));
				return result;
			}, null, this);
			return result != null ? result : defaultValue;
		}
	};
	var ViewModel = koclasses.ViewModel;


	var koutils = {

		/**
		 * Resolve paths of viewmodel to values.
		 *
		 * Can input one or more paths and a default value (if none of paths does not return value).
		 * If there is only one argument, it is treated as path.
		 * If there are more, than one argument, the last is treated as default value.
		 */
		resolve: ViewModel.prototype.resolve,
		
        enableUserPrompts: function() {
            userPrompts = true;
        },

        disableUserPrompts: function() {
            userPrompts = false;
        },

		renderTemplate: function(template, viewModel) {
			return YAHOO.lang.substitute(template, viewModel, function(key, accessor) {
				var value = accessor();
				assert(value != null, "Property (" + key + ") is required for template (" + template + ")", true);
				return _.isArray(value) ? value.join(",") : value;
			});
		},

		simpleLoad: function(config) {
			if(typeof config == "string") {
				config = { url: config };
			}

			assert(config.url != null, "url is required in simpleLoad", true);
			
			return function(viewModel) {
                try {
                    var url = _.isFunction(config.url) 
                            ? config.url.call(viewModel, viewModel) 
                            : koutils.renderTemplate(config.url, viewModel);
                } catch(e) {
                    return;
                }
				
				var callback = function(response) {
					var model = response.json;

					if (config.resultsPath) {
						model = Citeck.utils.resolvePath(model, config.resultsPath);
					}

					if (config.resultsMap) {
						model = Citeck.utils.mapObject(model, config.resultsMap);
					}

					if (config.postprocessing && typeof config.postprocessing == "function") {
						config.postprocessing.call(viewModel, model);
					}

					viewModel.model(model);
				};

				if(simpleLoadCache[url]) {
					simpleLoadCache[url].push(callback);
					return;
				}
				simpleLoadCache[url] = [callback];
				
				Alfresco.util.Ajax.request({
					method: config.method || "GET",
					url: url,
					successCallback: {
						fn: function(response) {
							_.each(simpleLoadCache[url], function(callback) {
								callback(response);
							});
							delete simpleLoadCache[url];
						}
					},
					failureCallback: {
						fn: function(response) {
							delete simpleLoadCache[url];
							fail("Failure response for request: " + url);
						}
					}
				});
			};
		},

		simpleSave: function(config) {
			assert(config.url != null, "url is required in simpleSave");
			return function(viewModel, callback, failureCallback) {
				var url = _.isFunction(config.url) 
				        ? config.url(viewModel) 
				        : koutils.renderTemplate(config.url, viewModel),
					request = config.toRequest ? config.toRequest(viewModel) : "";
				Alfresco.util.Ajax.request({
					method: config.method || "POST",
					url: url,
					dataObj: request,
					requestContentType: Alfresco.util.Ajax.JSON,
					successCallback: {
						fn: function(response) {
							var result = response.json;
							if(config.toResult) result = config.toResult(result);
							if(config.onSuccess) config.onSuccess(viewModel, result);
							if(typeof callback == "function") {
								callback(result);
							} else {
								callback.fn.call(callback.scope, result);
							}
						}
					},
					failureCallback: {
						fn: function(response) {
							// somehow response.json is not available for failure response
							var json = Alfresco.util.parseJSON(response.serverResponse.responseText);
							var message = config.toFailureMessage && config.toFailureMessage(json) || "Failure response for request: " + url;
							if(config.onFailure) config.onFailure(viewModel, message);
							switch(typeof failureCallback) {
							case "function": 
								return failureCallback(message);
							case "object": 
								return failureCallback.fn.call(failureCallback.scope, message);
							default:
                                return fail(message.substring(message.indexOf("\n") + 1));
							}
						}
					}
				});

			};
		},

		bulkLoad: function(loader, key, field) {
			return function(viewModel) {
				loader.url = koutils.renderTemplate(loader.url, viewModel);
				loader.load(viewModel[key](), function(id, model) {
					viewModel.model(field ? _.object([field],[model]) : model);
				});
			};
		},

		subscribeOnce: function(subscribable, callback, scope) {
			var subscription = subscribable.subscribe(function(newValue) {
				callback.call(scope, newValue);
				subscription.dispose();
			});
		},
		
		instanceOf: function(value, valueClass) {
		    if(value instanceof Object) {
		        return value instanceof valueClass;
		    } else {
		        return new Object(value) instanceof valueClass;
		    }
		},
		
		instantiate: function(data, valueClass) {
		    if(data == null || valueClass == null || koutils.instanceOf(data, valueClass)) {
		        return data;
		    } else if(valueClass == Boolean) {
		        // new Boolean("false") == true
		        return data != "false" && data != false && data != 0;
		    } else {
		        return new valueClass(data);
		    }
		},

		onDemandObservable: function(initialValue, loadCallback, valueClass) {
			var isArray = _.isArray(initialValue),
				value = isArray ? ko.observableArray(initialValue) : ko.observable(initialValue),
				loaded = ko.observable(loadCallback == null),
				loading = false,
				instanceOf = koutils.instanceOf;

			var result = ko.computed({
				read: function() {
					if(!loaded()) {
						if(loadCallback == null) {
							logger.warn("Can not load value, as there is no load callback");
						} else if(loading) {
							logger.info("We are already loading");
						} else {
							loading = true;
							loadCallback.fn.call(loadCallback.scope, this);
						}
					}
					return value();
				},
				write: function(newValue) {
					if(newValue != null && valueClass) {
						if(_.isArray(newValue)) {
							assert(_.every(newValue, _.partial(instanceOf, _, valueClass)), 
								"Supplied array (" + newValue + ") is not an array of required class (" + valueClass + ")");
						} else {
							assert(instanceOf(newValue, valueClass), 
								"Supplied value (" + newValue + ") is not an instance of required class (" + valueClass + ")");
						}
					}
					value(newValue);
					loaded(true);
					loading = false;
				},
				deferEvaluation: true
			});
			// export stuff:
			result._class = valueClass;
			result._value = value;
			result.loaded = loaded;
			result.reload = function() {
				loaded(false);
			};
			result.clone = function() {
				var cloneValue = function(orig) {
					if(_.isArray(orig)) return _.map(orig, cloneValue);
					if(_.result(valueClass, 'key')) return orig;
					if(orig != null && typeof orig.clone == "function") return orig.clone();
					return orig;
				};
				if(loaded()) {
					return koutils.onDemandObservable(cloneValue(value()), null, valueClass);
				}
				if(!loadCallback) {
					logger.warn("Can not clone non-loaded value with no default callback");
					return koutils.onDemandObservable(value(), null, valueClass);
				}
				var copy = koutils.onDemandObservable(value(), {
					fn: function(that) {
						result.peek(); // initiate loading
					}
				}, valueClass);
				koutils.subscribeOnce(value, function(newValue) {
					copy(cloneValue(newValue));
				});
				return copy;
			};
			// export observableArray special functions
			if(isArray) {
				_.each([ 'push', 'pop', 'unshift', 'shift', 'reverse', 'sort', 'splice', 'remove', 'removeAll' ], function(method) {
					result[method] = value[method].bind(value);
				});
			}
			return result;
		},
		
		numberSerializer: function(numberVariableName) {
		    return {
		        read: function() {
		            var numberValue = this[numberVariableName]();
		            return numberValue != null ? numberValue.toString() : "";
		        },
		        write: function(value) {
		            var numberValue = parseInt(value);
		            if(numberValue != NaN) {
		                this[numberVariableName](numberValue);
		            }
		        }
		    };
		},

		koclass: function(className, parentClass) {
			if(_.isFunction(koclasses[className])) {
				return koclasses[className];
			}
			var properties = {},
				computedProperties = {},
				nativeProperties = {},
				methods = {},
				attributeDefinitions = {
					"Property": {
						collection: properties,
						definitionTypes: [ "function", "array" ]
					},
					"Computed": {
						collection: computedProperties,
						definitionTypes: [ "function", "object" ]
					},
					"Method": {
						collection: methods,
						definitionTypes: [ "function" ]
					},
					"Native": {
					    collection: nativeProperties,
					    definitionTypes: [ "function", "object" ]
					}
				},
				assertNewAttribute = function(attributeType, newName, definition) {
					assert(_.isString(newName), attributeType + " name should be string, " + typeof(newName) + " provided instead");
					var definitionTypes = attributeDefinitions[attributeType].definitionTypes;
					var definitionType = _.isArray(definition) ? "array" : typeof definition;
					assert(definitionTypes.indexOf(definitionType) != -1, 
						attributeType + " definition should be " + definitionTypes.join(" or ") + ", " + definitionType + " provided instead");
					_.each(attributeDefinitions, function(attributeDef, attributeType) {
						assert(_.isUndefined(attributeDef.collection[newName]), 
							attributeType + " with name " + newName + " is already defined");
					});
				},
				initializers = [],
				constructors = [],
				defaultConstructors = [],
				objects = {},
				extenders = {},
				loaders = {},
				saveFn,
				removeFn,
				keyProperty,
				ViewModelClass = function(model) {
				    // protect from calling without 'new'
				    // TODO is it possible to pass any number of parameters in constructor?
					if(!(this instanceof ViewModelClass)) return new ViewModelClass(model);

					// choose constructor:
					var constructorArgs = arguments,
					    constructorMatch = function(constructor) {
					        var requiredTypes = constructor.args,
					            index = 0;
					        return constructorArgs.length == requiredTypes.length 
					        && _.all(requiredTypes, function(requiredType) {
					            return koutils.instanceOf(constructorArgs[index++], requiredType);
					        });
					    }
					var constructor = _.find(constructors, constructorMatch);
					var defaultConstructor = _.find(defaultConstructors, constructorMatch);
					assert(constructor != null || defaultConstructor != null, "Can not find constructor for specified arguments");
					
					var viewModel = this;
					if(constructor == null || constructor.init && defaultConstructor != null) {
					    viewModel = defaultConstructor.body.apply(viewModel, arguments) || viewModel;
					}
					if(constructor != null) {
					    viewModel = constructor.body.apply(viewModel, arguments) || viewModel;
					}
					
					// if it is the same new instance:
					if(viewModel === this) {
					    _.each(initializers, function(initializer) {
					        initializer.call(this, this);
					    }, viewModel);
					}
					
					return viewModel;
				}, 
				F = function() {},
				parentProto = F.prototype = (parentClass || ViewModel).prototype,
				proto = new F(),
				instantiate = koutils.instantiate,
				defaultConstructor = function() {
                    var viewModel = ViewModelClass.superclass.constructor.call(this);
                    viewModel.thisclass = ViewModelClass;
    
                    _.each(properties, function(type, name) {
                        var isArray = _.isArray(type);
                        type = isArray ? type[0] : type;
    
                        var loadCallback = {
                            scope: this,
                            fn: function() {
                                logger.debug("Loading " + name + " for " + this);
                                var loader = loaders[name] || loaders['*'];
                                if(!loader) {
                                    logger.warn("No loaders found for " + name);
                                    return;
                                }
                                loader.call(this, this);
                            }
                        };
    
                        this[name] = isArray
                            ? koutils.onDemandObservable([], loadCallback, type) 
                            : koutils.onDemandObservable(null, loadCallback, type);
                        if(extenders['*']) this[name].extend(extenders['*']);
                        if(extenders[name]) this[name].extend(extenders[name]);
                        this[name].toString = function() { return name; }
                    }, viewModel);
    
                    _.each(computedProperties, function(definition, name) {
                        if(_.isFunction(definition)) {
                            definition = {
                                read: definition
                            };
                        }
                        this[name] = ko.computed(_.extend(definition, {
                            owner: this,
                            deferEvaluation: true
                        }));
                        this[name].loaded = _.constant(true);
                        if(extenders['*']) this[name].extend(extenders['*']);
                        if(extenders[name]) this[name].extend(extenders[name]);
                        this[name].toString = function() { return name; }
                    }, viewModel);
    
                    _.each(nativeProperties, function(definition, name) {
                        definition = _.clone(definition);
                        if(definition.cache) {
                            // create computed
                            var computed = ko.computed({
                                owner: this,
                                read: definition.get,
                                write: definition.set,
                                deferEvaluation: true
                            }).extend({ rateLimit: { timeout: 0, method: "notifyWhenChangesStop" } })
                            //}).extend({ deferred: true })
                            definition.get = computed;
                            if(definition.set) {
                                definition.set = computed;
                            }
                            delete definition.cache;
                        } else {
                            // make property without computed
                            definition.get = _.bind(definition.get, this);
                            if(definition.set) {
                                definition.set = _.bind(definition.set, this);
                            }
                        }
                        Object.defineProperty(this, name, definition);
                    }, viewModel);
    
                    viewModel.$super = (function() {
                        var $super = {
                            $super: viewModel.$super
                        };
                        _.each(parentProto, function(method, methodName) {
                            if(!_.isFunction(method)) return;
                            $super[methodName] = function() {
                                method.apply(viewModel, arguments);
                            };
                        });
                        return $super;
                    })();
                    return viewModel;
                },
                keyConstructor = function(key) {
                    if(keyProperty == null) {
                        return ViewModelClass.superclass.constructor.call(this, key);
                    }
                    assert(koutils.instanceOf(key, properties[keyProperty]), "Passed key of wrong type: " + key);
                    if(objects[key]) {
                        logger.debug('Getting ' + className + '[' + keyProperty + '=' + key + '] from cache');
                        return objects[key];
                    } else {
                        defaultConstructor.call(this);
                        this.model(_.object([keyProperty], [key]));
                        logger.debug('Putting ' + className + '[' + keyProperty + '=' + key + '] in cache');
                        return objects[key] = this;
                    }
                },
                modelConstructor = function(model) {
                    var viewModel = null,
                        key = model[keyProperty];
                    if(keyProperty != null && key != null) {
                        viewModel = keyConstructor.call(this, key);
                    } else {
                        viewModel = defaultConstructor.call(this);
                    }
                    viewModel.model(model);
                    return viewModel;
                },
                copyConstructor = function(proto) {
                    defaultConstructor.call(this);
                    // make clone object
                    _.each(properties, function(type, name) {
                        var isArray = _.isArray(type),
                            accessor = proto[name];
                        type = isArray ? type[0] : type;
    
                        // process key value
                        if(name == keyProperty) {
                            this[name](null);
                            return;
                        }
    
                        // regular fields: clone
                        this[name] = accessor.clone();
                    }, this);
                    return this;
                };

			defaultConstructors.push(
			        {
			            args: [],
			            body: defaultConstructor
			        },
			        {
			            args: [ ViewModelClass ],
			            body: copyConstructor,
			            init: true
			        },
			        {
			            args: [ String ],
			            body: keyConstructor
			        },
			        {
			            args: [ Number ],
			            body: keyConstructor
			        },
			        {
			            args: [ Object ],
			            body: modelConstructor
			        }
			);

				
			// common prototype extensions
			_.extend(proto, {
				constructor: ViewModelClass,
				getModel: function() {
					var model = ViewModelClass.superclass.getModel.apply(this, arguments);
					_.each(properties, function(type, name) {
						var isArray = _.isArray(type),
							value = this[name].peek(),
							toModel = function(value) {
								if(value == null) {
									return null;
								}
								var key = _.result(type.key);
								if(key) {
									return value[key]();
								}
								if(type.prototype.model != null) {
									return value.model();
								}
								return value;
							};
						type = isArray ? type[0] : type;
						model[name] = isArray ? _.map(value, toModel) : toModel(value);
					}, this);
					return model;
				},
				setModel: function(model) {
					ViewModelClass.superclass.setModel.apply(this, arguments);
					_.each(model, function(value, name) {
						if(!properties.hasOwnProperty(name)) {
							// ignore attribute not present in model definition
							return;
						}
						var type = properties[name],
							isArray = _.isArray(type);
						type = isArray ? type[0] : type;
						if(isArray) {
							value = _.map(value, function(item) {
								return instantiate(item, type);
							});
						} else {
							value = instantiate(value, type);
						}
						this[name](value);
					}, this);
				}
			});

			// main definition methods
			_.extend(ViewModelClass, {
				prototype: proto,
				superclass: parentProto,
				key: function(propertyName, type) {
					if(arguments.length == 0) return keyProperty;
					// make all assertions before assigning keyProperty
					ViewModelClass.property(propertyName, type);
					keyProperty = propertyName;
					return ViewModelClass;
				},
				property: function(propertyName, type) {
					assertNewAttribute("Property", propertyName, type);
					properties[propertyName] = type;
					return ViewModelClass;
				},
				computed: function(propertyName, definition) {
					assertNewAttribute("Computed", propertyName, definition);
					computedProperties[propertyName] = definition;
					return ViewModelClass;
				},
				// if cache == true, the computed will be created for this property
				// otherwise it will be calculated each time
                nativeProperty: function(propertyName, definition, cache) {
                    assertNewAttribute("Native", propertyName, definition);
                    if(_.isFunction(definition)) {
                        definition = {
                            configurable: false,
                            get: definition,
                            cache: cache
                        }
                    }
                    nativeProperties[propertyName] = definition;
                    return ViewModelClass;
                },
				constant: function(propertyName, value) {
					var definition = _.constant(value);
					return ViewModelClass.computed(propertyName, definition);
				},
				shortcut: function(propertyName, resolveArguments) {
					resolveArguments = _.rest(arguments, 1);
					var definition = function() {
						return this.resolve.apply(this, resolveArguments);
					};
					return ViewModelClass.computed(propertyName, definition);
				},
				method: function(methodName, definition) {
					assertNewAttribute("Method", methodName, definition);
					methods[methodName] = definition;
					ViewModelClass.prototype[methodName] = definition;
					return ViewModelClass;
				},
				init: function(initializer) {
					initializers.push(initializer);
					return ViewModelClass;
				},
				constructor: function(args, body, init) {
				    constructors.push({
				        args: args,
				        body: body,
				        init: init
				    });
				    return ViewModelClass;
				},
				load: function(propertyName, loader) {
				    if(_.isArray(propertyName)) {
				        _.each(propertyName, function(propertyName) {
				            loaders[propertyName] = loader;
				        });
				    } else {
				        loaders[propertyName] = loader;
				    }
					return ViewModelClass;
				},
				save: function(arg) {
					if(arg instanceof ViewModelClass) {
						assert(_.isFunction(saveFn), "Save function must exist");
						saveFn.apply(this, arguments);
					} else if(_.isFunction(arg)) {
						saveFn = arg;
					} else {
						fail("Not supported argument type");
					}
					return ViewModelClass;
				},
				remove: function(arg) {
					if(arg instanceof ViewModelClass) {
						assert(_.isFunction(removeFn), "Remove function must exist");
						removeFn.apply(this, arguments);
					} else if(_.isFunction(arg)) {
						removeFn = arg;
					} else {
						fail("Not supported argument type");
					}
					return ViewModelClass;
				},
				extend: function(propertyName, newExtenders) {
					if(_.isUndefined(extenders[propertyName])) {
						extenders[propertyName] = {};
					}
					_.extend(extenders[propertyName], newExtenders);
					return ViewModelClass;
				},
			});

			// aliases and shortcuts
			var collectionAccessor = function(collection, defineOne) {
				return function() {
					if(arguments.length == 0) {
						return _.clone(collection);
					}
					_.each(arguments, function(definitions) {
						_.each(definitions, function(definition, name) {
							defineOne(name, definition);
						});
					});
					return ViewModelClass;
				};
			};

			_.extend(ViewModelClass, {
				properties: collectionAccessor(properties, ViewModelClass.property),
				computedProperties: collectionAccessor(computedProperties, ViewModelClass.computed),
				methods: collectionAccessor(methods, ViewModelClass.method),

				toString: _.constant(className),
				cache: objects,
			});
			return koclasses[className] = ViewModelClass;
		},

		generateViewModels: function(models, viewModels, extenders) {
			viewModels = viewModels || {};
			var simpleTypes = {
				"string": String,
				"number": Number,
				"boolean": Boolean,
				"object": Object
			};
			_.each(models, function(modelDef, name) {
				var ViewModelClass = viewModels[name] = function(model) {

					assert(model != null, "Can not create viewmodel from null");
					assert(!_.isArray(model), "Can not create viewmodel from array");
//					if(model instanceof ViewModelClass) return model;
					
					switch(typeof model) {
					case "function":
						model = model();
					case "string":
					case "number":
						assert(typeof ViewModelClass.key != "undefined",
							"Can not create object (" + name + ") from literal (" + model + "): the key is not defined");
						var newModel = {};
						newModel[ViewModelClass.key] = model;
						model = newModel;
						break;
					};

					// get object from cache, if available
					// cache object, if necessary
					var viewModel = this,
						key = model[ViewModelClass.key],
						isNew = true;
					if(typeof key == "string" || typeof key == "number") {
						if(ViewModelClass.cache[key]) {
							viewModel = ViewModelClass.cache[key];
							isNew = false;
							logger.debug('Getting ' + name + '[' + ViewModelClass.key + '=' + key + '] from cache');
						} else {
							ViewModelClass.cache[key] = viewModel;
							logger.debug('Putting ' + name + '[' + ViewModelClass.key + '=' + key + '] in cache');
						}
					} else if(typeof key == "undefined") {
					    model[ViewModelClass.key] = null; // set nullary key explicitly
					}

					if(isNew) {
						viewModel.thisclass = ViewModelClass;

						// special "model" property
						var modelRead = function() {
							var model = {};
							_.each(modelDef, function(attr, name) {
								var isArray = _.isArray(attr),
									itemType = isArray ? attr[0] : attr;
								if(!isArray && _.isObject(attr)) {
									return;
								}
								var value = this[name].peek();
								if(simpleTypes[itemType] || value == null) {
									model[name] = value;
									return;
								}
								var key = viewModels[itemType].key;
								if(isArray) {
									model[name] = _.map(value, function(obj) {
										return obj[key || 'model']();
									});
								} else {
									model[name] = value[key || 'model']();
								}
							}, this);
							return model;
						};
						var modelWrite = function(model) {
							_.each(model, function(value, name) {
								if(!modelDef.hasOwnProperty(name)) {
									// ignore attribute not present in model definition
									return;
								}
								var attr = modelDef[name],
									isArray = _.isArray(attr),
									itemType = isArray ? attr[0] : attr;

								// skip computed:
								if(!isArray && _.isObject(attr)) {
									return;
								}

								if(!simpleTypes[itemType] && value) {
									if(isArray) {
										value = _.map(value, function(item) {
											return new viewModels[itemType](item);
										});
									} else if(typeof itemType == "string") {
										value = new viewModels[itemType](value);
									}
								}
								this[name](value);
							}, this);
						};
					
						viewModel.model = function() {
							if(arguments.length == 0) {
								return modelRead.apply(viewModel, arguments);
							} else {
								return modelWrite.apply(viewModel, arguments);
							}
						};
						
						// defined properties
						_.each(modelDef, function(attr, name) {
							var isArray = _.isArray(attr),
								itemType = isArray ? attr[0] : attr;
							
							var loadCallback = {
								scope: this,
								fn: function() {
									logger.debug("Loading " + name + " for " + viewModel);
									ViewModelClass.load(viewModel, name);
								}
							};

							if(!isArray && _.isObject(attr)) {
								var functions = _.isFunction(attr) ? { read: attr } : attr;
								this[name] = ko.computed(_.extend(functions, {
									owner: this,
									deferEvaluation: true
								}));
								this[name].loaded = _.constant(true);
							} else if(isArray) {
								this[name] = koutils.onDemandObservable([], loadCallback, viewModels[itemType]);
							} else {
								this[name] = koutils.onDemandObservable(null, loadCallback, viewModels[itemType]);
							}
							if(extenders) {
								this[name].extend(extenders);
							}
							this[name].toString = function() {
								return name;
							}
						}, viewModel);
					}

					if(model instanceof ViewModelClass) {
						// make clone object
						_.each(modelDef, function(attr, name) {
							var isArray = _.isArray(attr),
								itemType = isArray ? attr[0] : attr,
								accessor = model[name];
							// skip computed
							if(!isArray && _.isObject(attr)) {
								return;
							}

							// process key value
							if(name == ViewModelClass.key) {
								this[name](null);
								return;
							}

							// regular fields: clone
							this[name] = accessor.clone();
						}, viewModel);
					} else {
						viewModel.model(model);
					}
					
					if(!isNew) return viewModel;

					if(typeof ViewModelClass.init == "function") {
						ViewModelClass.init(viewModel);
					}
					if(typeof ViewModelClass.notify == "function") {
						ViewModelClass.notify(viewModel);
					}
					return viewModel;
				};

				// ViewModel prototype methods
				YAHOO.extend(ViewModelClass, ViewModel);

				// ViewModel static methods 
				_.extend(ViewModelClass, {
					toString: _.constant(name),
					cache: {},
					load: function(viewModel, necessaryAttributes) {
						necessaryAttributes = _.flatten(_.rest(arguments, 1));
						// get loaders
						var loaders = _.uniq(_.compact(_.map(necessaryAttributes, function(attr) {
							return ViewModelClass.load[attr] || ViewModelClass.load['*'];
						})));
						if(loaders.length == 0) {
							logger.warn("No loaders found for " + necessaryAttributes.join(", "));
							return;
						}
						// execute loaders
						_.each(loaders, function(loader) {
							loader(viewModel);
						});
					}
				});
			});
			return viewModels;
		},

		subscribable: {
			
			// name of key field must be overriden by implementations
			key: null,
			
			bindOnce: true,
			
			subscribe: function(key, fn, scope) {
				assert(key, "key should be specified");
				assert(fn, "callback should be specified");
				this._init();
				var subscribers = this.subscribers,
					instances = this.instances,
					instance = instances[key];
				if(instance) {
					this._notify(instance, fn, scope);
					return true;
				} else {
					if(typeof subscribers[key] == "undefined") {
						subscribers[key] = [];
					}
					subscribers[key].push({
						fn: fn,
						scope: scope
					});
				}
			},
			
			notify: function(instance) {
				this._init();
				var keyField = this.key,
					key = typeof instance[keyField] == "function" 
					? instance[keyField]() 
					: instance[keyField],
					subscribers = this.subscribers[key];
				this.instances[key] = instance;
				if(subscribers) {
					_.each(subscribers, function(subs) {
						this._notify(instance, subs.fn, subs.scope);
					}, this);
					if(this.bindOnce) {
						delete this.subscribers[key];
					}
				}
			},
			
			_notify: function(instance, fn, scope) {
				fn.call(scope, instance);
			},
			
			_init: function() {
				YAHOO.lang.augmentObject(this, {
					// instances by key
					instances: {},
					// subscribers by key
					subscribers: {}
				});
			},
		},

	};
	
	koutils.Model = function() {
		
	};

	koutils.CachedModel = function(initialCaches) {
		koutils.CachedModel.superclass.constructor.call(this);
		this.caches = {};
		_.each(initialCaches || {}, function(cache, name) {
			if(cache) {
				this.addToCache(name, cache);
			}
		}, this);
	};

	YAHOO.extend(koutils.CachedModel, koutils.Model, {
		
		initCache: function(name) {
			var cache = this.caches[name];
			if(!cache) {
				cache = {};
				this.caches[name] = cache;
			}
			return cache;
		},

		addToCache: function(cacheName, values) {
			var cache = this.initCache(cacheName);
			_.each(values, function(value, key) {
				cache[key] = value;
			});
		},
		
	});
	
	ko.bindingHandlers.API = {
        init: function(element, valueAccessor, allBindings, viewModel, bindingContext) {
            // Make a modified binding context, with a extra properties, and apply it to descendant elements
            var innerBindingContext = bindingContext.extend(ko.unwrap(valueAccessor()));
            ko.applyBindingsToDescendants(innerBindingContext, element);
            // Also tell KO *not* to bind the descendants itself, otherwise they will be bound twice
            return { controlsDescendantBindings: true };
        }
    };
	ko.virtualElements.allowedBindings.API = true;
	
    ko.observable.fn.equalityComparer = ko.computed.fn.equalityComparer = function simpleComparer(a, b) {
        var aa = _.isArray(a),
            ba = _.isArray(b);
        if(aa != ba) return false;
        if(!aa) return a === b;
        if(a.length != b.length) return false;
        // note: if push, pop or other array functions are called on observableArray
        // equalityComparer is called with two equal arrays
        // so we have to return false notify about change
        if(a === b) return false;
        for(var i = a.length; i--; ) {
            if(a[i] !== b[i]) return false;
        }
        return true;
    };
	
	return koutils;
	
})