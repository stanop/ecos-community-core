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
/**
 * DocumentSubscribers document-details component.
 *
 * @class Citeck.widget.DocumentSubscribers
 */
if (typeof Citeck == "undefined" || !Citeck) {
    var Citeck = {};
}
if (typeof Citeck.widget == "undefined" || !Citeck.widget) {
    Citeck.widget = {};
}

(function() {

    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event,
        Element = YAHOO.util.Element;

    /**
     * DocumentSubscribers constructor
     *
     * @param htmlId - identifier of component root DOM element.
     */
    Citeck.widget.DocumentSubscribers = function(htmlId) {

        Citeck.widget.DocumentSubscribers.superclass.constructor.call(this, "Citeck.widget.DocumentSubscribers", htmlId, ["event","button"]);

    };

    YAHOO.extend(Citeck.widget.DocumentSubscribers, Alfresco.component.Base, {

        options: {

            /**
             * NodeRef of target object
             *
             * @property nodeRef
             * @type string
             */
            nodeRef: null,
			
            /**
             * association qname
             *
             * @property assocType
             * @type string
             */
            assocType: null,

            /**
             * current user name
             *
             * @property user
             * @type string
             */
            currentUserId: null

        },
		hasPermissionEdit: false,
		assocExists: false,
        /**
         * Event handler called when "onReady"
         *
         * @method: onReady
         */
        onReady: function() {
            var me = this;
			var currentUser = this.options.currentUserId;
			var isAssocs = false;
			var currentUserRef = "";
            this.hasPermissionEdit = false;
			var searchUrl = Alfresco.constants.PROXY_URI +'api/orgstruct/authority/'+currentUser;
			YAHOO.util.Connect.asyncRequest(
				'GET',
				searchUrl, 
				{
					success: function (response) 
					{
						if (response.responseText) 
						{
							var data = eval('(' + response.responseText + ')');
							currentUserRef = data['nodeRef'];
							var hasPermissionUrl = Alfresco.constants.PROXY_URI + 'citeck/has-permission?nodeRef=' + this.options.nodeRef+ '&permission=Read';
							YAHOO.util.Connect.asyncRequest(
								'GET',
								hasPermissionUrl, {
									success: function (response) {
										if (response.responseText.trim() == "true") {
											me.hasPermissionEdit = true;
										}
										if(this.hasPermissionEdit) {
											var searchUrl = Alfresco.constants.PROXY_URI + 'citeck/assocs?nodeRef=' + this.options.nodeRef+ '&assocTypes=' + this.options.assocType;
											YAHOO.util.Connect.asyncRequest(
											'GET',
											searchUrl, {
												success: function (response) {
													
													if (response.responseText) {
															var data = eval('({' + response.responseText + '})');
															var messageEl = Dom.get(this.id + "-message");
															for (var i=0; i<data.assocs[0].targets.length; i++) {
																	var userRef = data.assocs[0].targets[i]['nodeRef'];
																	if(userRef==currentUserRef) {
																		this.assocExists=true;
																}
															}
														}
														if(this.assocExists)
														{
															var unSubsrcibersButton = Dom.get(this.id + '-subsrcibers-button');
															var a = document.createElement('a');
															a.innerHTML = this.msg("link.unsubsrcibe");
															a.id = this.id + '-action-link';
															a.href = '#';
															a.title = this.msg("link.unsubsrcibe");
															a.tabIndex = '-1';
															Event.on(a, 'click', this.onAddDeleteAssociation, a, this);
															unSubsrcibersButton.innerHTML = this.msg("header.unsubsrcibe")+' ';
															unSubsrcibersButton.appendChild(a);
														}
														else
														{
															var subsrcibersButton = Dom.get(this.id + '-subsrcibers-button');
															var a = document.createElement('a');
															a.innerHTML = this.msg("link.subsrcibe");
															a.id = this.id + '-action-link';
															a.href = '#';
															a.title = this.msg("link.subsrcibe");
															a.tabIndex = '-1';
															Event.on(a, 'click', this.onAddDeleteAssociation, a, this);
															subsrcibersButton.innerHTML = this.msg("header.subsrcibe")+' ';
															subsrcibersButton.appendChild(a);
														}
													},
													scope: this
												}
											);
										}
									},
									scope: this
								}
							);
						} 
						
					},
					scope: this
				}
			);
		},
		
        onAddDeleteAssociation: function QB_onAddDeleteAssociation() {
            var me = this;
            var currentUserFullName = this.options.currentUserId;
            var user = "";
			var addUrl = "";
			var userRef = "";
			var data = "";
							var searchUrl = Alfresco.constants.PROXY_URI +'api/orgstruct/authority/'+currentUserFullName;
							YAHOO.util.Connect.asyncRequest(
                            'GET',
                            searchUrl, {
                                success: function (response) {
                                    if (response.responseText) {
											var data = eval('(' + response.responseText + ')');
											userRef = data['nodeRef'];

			            

			if (this.assocExists) {
				deleteUrl = Alfresco.constants.PROXY_URI + 'citeck/remove-assocs?sourceRef=' + me.options.nodeRef + '&targetRef=' + userRef +'&assocTypes=' + this.options.assocType;
                YAHOO.util.Connect.asyncRequest(
                    'DELETE',
                    deleteUrl, {
                        success: function (response) {
									if (response.responseText) {
										var data = eval('(' + response.responseText + ')');
										if (data.data) {
											this.assocExists=false;
											var subsrcibersButton = Dom.get(this.id + '-subsrcibers-button');
											var a = document.createElement('a');
											a.innerHTML = this.msg("link.subsrcibe");
											a.id = this.id + '-action-link';
											a.href = '#';
											a.title = this.msg("link.subsrcibe");;
											a.tabIndex = '-1';
											Event.on(a, 'click', this.onAddDeleteAssociation, a, this);
											subsrcibersButton.innerHTML = this.msg("header.subsrcibe")+' ';
											subsrcibersButton.appendChild(a);
										}
									}
                        },
                        failure: function(response) {
                            Alfresco.util.PopupManager.displayPrompt(
                                {
                                    title: this.msg("header.error"),
                                    text: this.msg("text.error"),
                                    noEscape: true,
                                    buttons: [
                                        {
                                            text: this.msg("button.ok"),
                                            handler: function dlA_onAction_cancel()
                                            {
                                                this.destroy();
                                            }
                                        }]
                                });
                        },
                        scope: me
                    });
			} else {
			addUrl = Alfresco.constants.PROXY_URI + 'citeck/add-assocs?sourceRef=' + me.options.nodeRef + '&targetRef=' + userRef +'&assocTypes=' + this.options.assocType;
                YAHOO.util.Connect.asyncRequest(
                    'POST',
                    addUrl, {
                        success: function (response) {
								if (response.responseText) {
										var data = eval('({' + response.responseText + '})');
										if (data.data) {
											this.assocExists=true;
											var unSubsrcibersButton = Dom.get(this.id + '-subsrcibers-button');
											var a = document.createElement('a');
											a.innerHTML = this.msg("link.unsubsrcibe");
											a.id = this.id + '-action-link';
											a.href = '#';
											a.title = this.msg("link.unsubsrcibe");
											a.tabIndex = '-1';
											Event.on(a, 'click', this.onAddDeleteAssociation, a, this);
											unSubsrcibersButton.innerHTML = this.msg("header.unsubsrcibe")+' ';
											unSubsrcibersButton.appendChild(a);
										}
									}
                        },
                        failure: function(response) {
                            Alfresco.util.PopupManager.displayPrompt(
                                {
                                    title: this.msg("header.error"),
                                    text: this.msg("text.error"),
                                    noEscape: true,
                                    buttons: [
                                        {
                                            text: this.msg("button.ok"),
                                            handler: function dlA_onAction_cancel()
                                            {
                                                this.destroy();
                                            }
                                        }]
                                });
                        },
                        scope: me
                    });
						}
					} 
						
					},
					scope: this
				}
			);
        }
    });
})();
