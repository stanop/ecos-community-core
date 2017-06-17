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
define(['lib/knockout', 'citeck/utils/knockout.utils'], function(ko, koutils) {
	
var logger = Alfresco.logger,
	PopupManager = Alfresco.util.PopupManager,
	lazyLoad = Citeck.utils.lazyLoad,
	subscribable = koutils.subscribable;

/* ------------------------------------ */
/* The model                            */
/* ------------------------------------ */

Citeck.forms.PassportsModel = function(initialCaches) {
	Citeck.forms.PassportsModel.superclass.constructor.call(this, initialCaches);
};

YAHOO.extend(Citeck.forms.PassportsModel, koutils.CachedModel, {
	
	loadPeople: function(nodeRefs, callback) {
		var cache = this.initCache('people');
		lazyLoad(cache, nodeRefs, 
				Alfresco.constants.PROXY_URI + "api/orgstruct/authorities-explicit?nodeRefs={keys}",
				callback, null, "nodeRef");
	},
	
	loadPassports: function(userName, callback) {
		var cache = this.initCache('passports');
		lazyLoad(cache, userName,
				Alfresco.constants.PROXY_URI + "citeck/passports?userName={key}", 
				callback);
	},
	
	unloadPassports: function(userName) {
		var cache = this.initCache('passports');
		delete cache[userName];
	},
	
});

/* ------------------------------------ */
/* The viewmodel                        */
/* ------------------------------------ */

var VM = (function() {
	var s = "string",
		n = "number",
		b = "boolean",
		o = "object";
	return koutils.generateViewModels({
		Person: {
			nodeRef: s,
			fullName: s,
			firstName: s,
			lastName: s,
			displayName: function() {
				var names = _.compact([ 
                	this.firstName(), 
                	this.lastName() 
                ]);
				return (names.length > 0) 
					? names.join(" ") 
					: this.fullName();
			}
		},
		Passport: {
			nodeRef: s,
			userName: s,
			info: s,
			date: s,
			canRead: b,
			canWrite: b,
			viewDate: function() {
				var dateISO = this.date();
				if(!dateISO) return "";
				var date = Alfresco.util.fromISO8601(dateISO);
				return date.toString("dd.MM.yyyy");
			}
		},
		PassportSet: {
			userName: s,
			passports: ["Passport"],
			bestMatchingPassport: function() {
				var passports = this.passports();
				if(passports && passports.length) {
					return passports[0];
				} else {
					return null;
				}
			}
		},
		Record: {
			person: "Person",
			passports: "PassportSet",
			passport: "Passport"
		},
		PassportsTable: {
			records: [ "Record" ],
			
			// hidden fields interface
			peopleIdsOriginal: [ s ],
			peopleIds: function() {
				return _.map(this.records(), function(record) {
					return record.person().nodeRef();
				});
			},
			peopleIdsAdded: function() {
				return _.difference(this.peopleIds(), this.peopleIdsOriginal());
			},
			peopleIdsRemoved: function() {
				return _.difference(this.peopleIdsOriginal(), this.peopleIds());
			},
			
			passportIdsOriginal: [ s ],
			passportIds: function() {
				return _.compact(_.map(this.records(), function(record) {
					return record.passport() && record.passport().nodeRef();
				}));
			},
			passportIdsAdded: function() {
				return _.difference(this.passportIds(), this.passportIdsOriginal());
			},
			passportIdsRemoved: function() {
				return _.difference(this.passportIdsOriginal(), this.passportIds());
			},
			
			allPassportsPresent: function() {
				return _.every(this.records(), function(record) {
					return record.passport() != null;
				});
			},
			
		}
	});
})();

var PassportsControl = function(htmlid, name) {
	Citeck.forms.PassportsControl.superclass.constructor.call(this, name || 'Citeck.forms.PassportsControl', htmlid);
	VM.PassportsTable.call(this, {
		records: []
	});
};

YAHOO.extend(PassportsControl, Alfresco.component.Base, {
	
	onReady: function() {
		this.widgets.peopleDialog = new Citeck.widget.DynamicTreePicker(this.id + "-picker");
		this.widgets.peopleDialog.setOptions(this.options.peopleDialogOptions);
		this.widgets.peopleDialog.subscribe("itemsSelected", this.onPeopleSelected, this, true);
		
		this.model = new Citeck.forms.PassportsModel();
		
		this.peopleIdsOriginal(this.options.peopleIdsOriginal);
		this.passportIdsOriginal(this.options.passportIdsOriginal);
		
		this.model.loadPeople(this.options.peopleIds || this.options.peopleIdsOriginal, {
			scope: this,
			fn: this.onPeopleSelected
		})
		
		ko.applyBindings(this, Dom.get(this.id));
	},
	
	onSelectPeopleClick: function() {
		this.widgets.peopleDialog.setSelectedItems(_.map(this.peopleIds(), function(nodeRef) {
			return {
				// TODO refactor this
				_item_name_: 'authority-' + nodeRef,
				nodeRef: nodeRef 
			};
		}));
		this.widgets.peopleDialog.show();
	},
	
	onPeopleSelected: function(selected) {
		// figure out records to be removed
		// and records to be added
        var people = {};
        for(var i in selected) {
            var fullName = selected[i].fullName;
            if(fullName.indexOf("GROUP_") == 0) {
                var url = Alfresco.constants.PROXY_URI + "api/orgstruct/group/" + fullName.replace('GROUP_','') + "/children?&user=true&recurse=true&role=false&branch=false&group=false";
                var xmlHttp = new XMLHttpRequest();
                xmlHttp.open( "GET", url, false );
                xmlHttp.send( null );
                var users = eval('(' + xmlHttp.responseText + ')');
                for (var j in users ) {
                    if(!people[users[j].fullName]) {
                        people[users[j].fullName] = users[j];
                    }
                }
            } else {
                if(!people[fullName]) {
                    people[fullName] = selected[i];
                }
            }
        }
        var selectedPeople = Object.keys(people).map(function(k){return people[k]});
		var oldIds = this.peopleIds(),
			newIds = _.pluck(selectedPeople, 'nodeRef'),
			idsToAdd = _.difference(newIds, oldIds),
			idsToRemove = _.difference(oldIds, newIds);
		_.each(idsToRemove, function(nodeRef) {
			var record = this.findRecordByPersonNodeRef(nodeRef);
			if(record) this.records.remove(record);
		}, this);
		_.each(idsToAdd, function(nodeRef) {
			var person = _.findWhere(selectedPeople, { nodeRef: nodeRef });
			var record = new VM.Record({
				person: person
			});
			
			this.invalidateRecord(record, null);
			
			this.records.push(record);
		}, this);
	},
	
	findRecordByPersonNodeRef: function(personNodeRef) {
		return _.find(this.records(), function(record) {
			return record.person().nodeRef() == personNodeRef;
		});
	},
	
	invalidateRecord: function(record, selectedPassportNodeRef) {
		var userName = record.person().fullName();
		record.passports(null);
		record.passport(null);
		this.model.unloadPassports(userName);
		this.model.loadPassports(userName, {
			scope: this,
			fn: function(data) {
				data.passports = _.sortBy(data.passports, 'date').reverse();
				var passports = new VM.PassportSet(data);
				record.passports(passports);
				
				var passport = null;
				if(selectedPassportNodeRef) {
					passport = _.find(record.passports().passports(), function(passport) {
						return passport.nodeRef() == selectedPassportNodeRef;
					});
				} else {
					var original = this.passportIdsOriginal();
					passport = _.find(record.passports().passports(), function(passport) {
						return _.contains(original, passport.nodeRef());
					});
				}
				
				if(passport) { 
					record.passport(passport);
				}
			}
		});
	},
	
	addPassport: function(record) {
		var personNodeRef = record.person().nodeRef();
		var dialog = new Alfresco.module.SimpleDialog(this.id + "-" + Alfresco.util.generateDomId());
		dialog.setOptions({
			width: '40em', 
			templateUrl: Alfresco.constants.URL_SERVICECONTEXT + 'components/form?itemKind=type&itemId=pass:passport&mode=create&showSubmitButton=true&showCancelButton=true&assoc_pass_person=' + personNodeRef + '&destination=' + this.options.passportsDestination, 
			actionUrl: null,
			destroyOnHide: false,
            doBeforeDialogShow: {
                fn: function(p_form, p_dialog) {
                    var titleSpan = '<span class="light">' + this.msg("passports.header.add-passport") + '</span>';
                    Alfresco.util.populateHTML([ p_dialog.id + '-form-container_h', titleSpan ]);
                },
                scope: this
            },
			onSuccess: {
				scope: this,
				fn: function(response) {
					this.invalidateRecord(record, response.json.persistedObject);
				}
			},
            onFailure: {
                scope: dialog,
                fn: this.onFailure
            }
		});
		dialog.show();
	},

    onFailure: function(response) {
        this.widgets.cancelButton.set('disabled', false);
        var message = response.json.message.split("\n").slice(1).join("\n");
        Alfresco.util.PopupManager.displayPrompt(
            {
                title: this.msg("message.failure"),
                text: message
            });
    },
	
	viewPassport: function(record) {
		PopupManager.displayForm({
			title: this.msg("passports.header.view-passport"),
			properties: {
				itemKind: 'node',
				itemId: record.passport().nodeRef(),
				formId: 'preview',
				mode: 'view'
			}
		});
	},
	
	editPassport: function(record) {
		var passportNodeRef = record.passport().nodeRef();
		var dialog = new Alfresco.module.SimpleDialog(this.id + "-" + Alfresco.util.generateDomId());
		dialog.setOptions({
			width: '40em', 
			templateUrl: Alfresco.constants.URL_SERVICECONTEXT + 'components/form?itemKind=node&itemId=' + passportNodeRef + '&mode=edit&showSubmitButton=true&showCancelButton=true',
			actionUrl: null,
			destroyOnHide: false,
            doBeforeDialogShow: {
                fn: function(p_form, p_dialog) {
                    var titleSpan = '<span class="light">' + this.msg("passports.header.edit-passport") + '</span>';
                    Alfresco.util.populateHTML([ p_dialog.id + '-form-container_h', titleSpan ]);
                },
                scope: this
            },
			onSuccess: {
				scope: this,
				fn: function(response) {
					this.invalidateRecord(record, response.json.persistedObject);
				}
			},
            onFailure: {
                scope: dialog,
                fn: this.onFailure
            }
		});
		dialog.show();
	},
	
});

return PassportsControl;
})