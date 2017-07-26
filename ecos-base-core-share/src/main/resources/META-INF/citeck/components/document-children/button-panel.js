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
(function() {


	Citeck = typeof Citeck != "undefined" ? Citeck : {};
	Citeck.widget = Citeck.widget || {};

	/**
	 * It represents the specified actions.
	 * The list of actions is specified by string {@code actionKeys},
	 * which contains actions divided by comma.
	 *
	 *
	 */
	Citeck.widget.ButtonPanel = function(htmlid) {
		Citeck.widget.ButtonPanel.superclass.constructor.call(this, "Citeck.widget.ButtonPanel", htmlid, null);
		this.loaded = false;
	};

	YAHOO.extend(Citeck.widget.ButtonPanel, Alfresco.component.Base);
	YAHOO.lang.augmentObject(Citeck.widget.ButtonPanel.prototype, {
		// default values for options
		options: {
			args: {
				// parent node reference
				nodeRef: null,

				// content type of uploaded or created documents
				contentType: null,

				// child association type of represented documents
				assocType: null,

				// it is a flag, if it is true, you can choose only only one file in uploading dialog
				singleSelectMode: false,

				// it is a string of a list of keys, each item is a name of action separated by comma.
				// current module shows only that actions which specified in this option.
				actionKeys: null
			}
		},

		/**
		 * Set options for this action panel
		 */
		setOptions: function(options) {
			Citeck.widget.ButtonPanel.superclass.setOptions.call(this, options);
			return this;
		},

		/**
		 * Set messages for this action panel
		 *
		 * Supported messages:
		 * message.action.panel.
		 */
		setMessages: function(messages) {
			Citeck.widget.ButtonPanel.superclass.setMessages.call(this, messages);
			return this;
		},

		onReady: function() {
			if (this.id.indexOf("agenda") != -1) console.log(this)

			if (this.options.args.buttonsInHeader) {
                var actionKeys = this.options.args.buttonsInHeader.split(',');

                if (this.options.args.availableButtonForGroups) {
                    var groups  = this.options.args.availableButtonForGroups.split(','),
						available = false;
					for (var i = 0; i < groups.length; i++) {
						Alfresco.util.Ajax.jsonGet({
							url: (Alfresco.constants.PROXY_URI + "citeck/is-group-member?userName=" + Alfresco.constants.USERNAME + "&groupName=" + groups[i]),
							successCallback: {
                                scope: this,
                                fn: function (response) {
                                    if (response.json && !available) {
                                        available = true;
                                        for (var i = 0; i < actionKeys.length; i++) {
                                            var key = actionKeys[i];
                                            if (key) {
                                                key = key.replace(/^\s+|\s+$/g, '');
                                                if (key)
                                                    this.renderPanelAction(key);
                                            }
                                        }
                                    }
                                }
                            }
						});
					}
				} else {
                    for (var i = 0; i < actionKeys.length; i++) {
                        var key = actionKeys[i];
                        if (key) {
                            key = key.replace(/^\s+|\s+$/g, '');
                            if (key)
                                this.renderPanelAction(key);
                        }
                    }
				}
			}

			this.loaded = true;
		},

		renderPanelAction: function(key) {
			var scope = this,
					title = this.msg('title.' + key),
					url = '/share/res/citeck/components/document-children/images/' + key + '-16.png',
					id = this.id + '-' + key;
					element = document.createElement('A');

			element.setAttribute('id', id);
			element.setAttribute('title', title);
			element.setAttribute('style', 'background-image:url(' + url + ')');
			element.innerHTML = '&nbsp;';

			var parent = YAHOO.util.Dom.get(this.id);
			parent.appendChild(element);

			if (Citeck.widget.ButtonPanel.Commands && typeof Citeck.widget.ButtonPanel.Commands[key] === "function") {
				YAHOO.util.Event.on(element, "click", function() {
					Citeck.widget.ButtonPanel.Commands[key](scope.options.args, scope);
				});
			}
		},

		removePanelAction: function(key) {
			var element = document.getElementById(this.id + '-' + key);
			if (element) element.remove();
		}

	});


})();
