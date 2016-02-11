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
define(['lib/jquery.simulate', 'citeck/utils/knockout.utils',
		'citeck/components/journals2/journals'], function(jqsim, koutils, Journals) {

var PopupManager = Alfresco.util.PopupManager,
	koclass = koutils.koclass,
	JournalsWidget = koclass('JournalsWidget'),
	JournalsPage = koclass('JournalsPage', JournalsWidget),
	Record = koclass('Record'),
	jqSimulate = $('').simulate;

JournalsPage
	// menu
	.property('currentMenu', String)
	.method('toggleToolbarMenu', function(menu) {
		if(this.currentMenu() == menu) {
			this.currentMenu('');
		} else {
			this.currentMenu(menu);
		}
	})

	// actions support
	.method('executeAction', function(action) {
		var vms = this.selectedRecords(), 
			records = [];
		for(var i in vms) {
			var vm = vms[i],
				loaded = vm.doclib.loaded(),
				record = vm.doclib();
			if(record) {
				records.push(record);
			} else if(!loaded) {
				koutils.subscribeOnce(vm.doclib, _.partial(this.executeAction, action), this);
				return;
			} else {
				throw new Error("doclib actions can be executed only on doclib nodes");
			}
		}
		this.actionsRuntime[action.id()](records);
	})

	// add user interaction for save and remove methods:
	.method('saveFilter', function() {
		this.userInteraction.simulateChange();
		if(!this._filter().valid()) return;
		this.userInteraction.askTitle({
			callback: { 
				scope: this,
				fn: function(title) {
					this._filter().title(title);
					this.$super.saveFilter();
				} 
			},
			title: this.msg("title.save-filter"),
			text: this.msg("label.save-filter"),
		});
	})
	.method('removeFilter', function(filter) {
		this.userInteraction.askConfirm({
			callback: {
				scope: this,
				fn: function() {
					this.$super.removeFilter(filter);
				},
			},
			text: this.msg("message.confirm.delete", filter.title()),
			title: this.msg("message.confirm.delete.1.title"),
		});
	})
	.method('saveSettings', function() {
		if(!this._settings().valid()) return;
		this.userInteraction.askTitle({
			callback: { 
				scope: this,
				fn: function(title) {
					this._settings().title(title);
					this.$super.saveSettings();
				} 
			},
			title: this.msg("title.save-settings"),
			text: this.msg("label.save-settings"),
		});
	})
	.method('removeSettings', function(settings) {
		this.userInteraction.askConfirm({
			callback: {
				scope: this,
				fn: function() {
					this.$super.removeSettings(settings);
				},
			},
			text: this.msg("message.confirm.delete", settings.title()),
			title: this.msg("message.confirm.delete.1.title"),
		});
	})
	.method('removeRecord', function(record) {
		this.userInteraction.askConfirm({
			callback: {
				scope: this,
				fn: function() {
					this.$super.removeRecord(record);
				},
			},
			text: this.msg("message.confirm.delete", record.attributes()['cm:name']),
			title: this.msg("message.confirm.delete.1.title"),
		});
	})
	.method('removeRecords', function(records) {
		this.userInteraction.askConfirm({
			callback: {
				scope: this,
				fn: function() {
					this.$super.removeRecords(records);
				},
			},
			text: this.msg("message.confirm.delete.description", records.length),
			title: this.msg("message.confirm.delete.title"),
		});
	})
	.method('performSearch', function() {
		this.userInteraction.simulateChange();
		this.$super.performSearch();
	})
	.method('applyCriteria', function() {
		this.userInteraction.simulateChange();
		this.$super.applyCriteria();
	})
	;
	
var JournalsPageWidget = function(htmlid) {
	JournalsPageWidget.superclass.constructor.call(this, 
		"Citeck.widgets.JournalsPage", 
		htmlid, 
		["button", "menu", "history", "paginator", "dragdrop"],
		JournalsPage);

	// doclib actions parameters:
	this.currentPath = "/";
	this.options.containerId = "documentLibrary",
	this.options.rootNode = "alfresco://company/home";

	this.viewModel.actionsRuntime = this;
	this.viewModel.userInteraction = this;
};

YAHOO.extend(JournalsPageWidget, Journals, {

	onReady: function() {
		JournalsPageWidget.superclass.onReady.apply(this, arguments);
		
		_.reduce([ 'metadataRefresh', 'fileDeleted', 'folderDeleted', 'filesDeleted' ], function(memo, eventName) {
			YAHOO.Bubbling.on(eventName, function(layer, args) {
				this.viewModel.performSearch()
			}, this);
		}, null, this);

		YAHOO.Bubbling.on('removeJournalRecord', function(layer, args) {
			this.viewModel.removeRecord(new Record(args[1]));
		}, this);
	},

	simulateChange: function() {
		// simulate change on hidden fields, 
		// to force view models update
		jqSimulate.call($('#' + this.id + '-filter-criteria input[type="hidden"]'), 'change');
	},

	askTitle: function(config) {
		return PopupManager.getUserInput(_.defaults(config, {
			input: "text",
			okButtonText: this.msg("button.save")
		}));
	},

	askConfirm: function(config) {
		var callback = config.callback;
		if(!callback) throw new Error("Callback should be specified");
		return PopupManager.displayPrompt(_.defaults(config, {
			buttons: [
				{
					text: this.msg("button.yes"),
					handler: function() {
						callback.fn.call(callback.scope);
						this.destroy();
					}
				},
				{
					text: this.msg("button.no"),
					handler: function() {
						this.destroy();
					},
					isDefault: true
				}
			]
		}));
	},

});

/*********************************************************/
/*           DOCUMENT LIBRARY ACTIONS SUPPORT            */
/*********************************************************/
	
_.extend(JournalsPageWidget.prototype, Alfresco.doclib.Actions.prototype, {

	// override for deleting multiple records
	onActionDelete: function(doclib) {
		if(_.isArray(doclib)) {
			this.viewModel.removeRecords(_.map(doclib, function(doclib) {
				return new Record(doclib.nodeRef);
			}));
		} else {
			this.viewModel.removeRecord(new Record(doclib.nodeRef));
		}
	},

});

return JournalsPageWidget;

})
