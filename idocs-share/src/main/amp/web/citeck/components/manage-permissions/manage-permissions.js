/*
 * Copyright (C) 2008-2016 Citeck LLC.
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

if (!MP) {
    var MP = {};
}

(function() {

    var $html = Alfresco.util.encodeHTML,
        $siteURL = Alfresco.util.siteURL,
        $userProfileLink = Alfresco.util.userProfileLink;

    MP.ManagePermissions = function(htmlId) {
        MP.ManagePermissions.superclass.constructor.call(this, "MP.ManagePermissions", htmlId);
        return this;
    };

    YAHOO.extend(MP.ManagePermissions, Alfresco.component.Base, {

        options: {
            nodeRef: null, // node, which permissions are edited
            authorityTable: null,
            inherit: null, // does the node inherit parent permissions
            authorityPermissions: {}, // current node ACL
            supportedPermissions: [], // available permissions
            permissionsMask: {}
        },

        createPermissionsMask: function MP_createPermissionsMask() {
            var gpList = this.options['supportedPermissions'];
            for (var i = 0; i < gpList.length; i++) {
                var pList = gpList[i]['permissions'];
                for (var j = 0; j < pList.length; j++) {
                    pList[j]['alias'] = pList[j]['alias'].trim();
                }
            }
            for (var i = 0; i < gpList.length; i++) {
                var pList = gpList[i]['permissions'];
                for (var j = 0; j < pList.length; j++) {
                    this.options.permissionsMask[pList[j]['alias']+'-ALLOWED'] = {
                        exist: false,
                        inherit: false
                    };
                    this.options.permissionsMask[pList[j]['alias']+'-DENIED'] = {
                        exist: false,
                        inherit: false
                    };
                }
            }
        },

        onReady: function MP_init() {
            this.createPermissionsMask();
            this.createAuthorityTable();
            this.updatePermissionsInfo();
            this.fillAuthorityTable();

            // Load the Authority Finder component
            Alfresco.util.Ajax.request({
                url: Alfresco.constants.URL_SERVICECONTEXT + "components/people-finder/authority-finder",
                dataObj: {
                    htmlid: this.id + "-authorityFinder"
                },
                successCallback: {
                    fn: this.onAuthorityFinderLoaded,
                    scope: this
                },
                failureMessage: this.msg("message.authorityFinderFail"),
                execScripts: true
            });
            this._updateInheritedUI();

            var _this = this;
            //change permission
            this.options.authorityTable.subscribe("checkboxClickEvent", function(oArgs) {
                var el = oArgs.target;
                _this.changePermission(el, el.checked, $(el).attr('accessType'), $(el).attr('authority'), $(el).attr('permission'));
            });
            //Buttons navigateForward
            this.widgets.inherited = Alfresco.util.createYUIButton(this, "inheritedButton", this.onInheritedButton);
            this.widgets.cancelButton = Alfresco.util.createYUIButton(this, "cancelButton", this.navigateForward);

            // Finally show the component body here to prevent UI artifacts on YUI button decoration
            Dom.setStyle(this.id + "-body", "visibility", "visible");
	        this.widgets.picker = new Citeck.widget.DynamicTreePicker(this.id + "-picker");
	        var childrenQuery = "branch=true&group=true&role=true&user=true";
	        var thisModel = {
		        formats: {
			        "authority": {
				        name: "{nodeRef}",
				        keys: [ "{fullName}", "selected-{selected}", "{authorityType}-{groupType}", "{authorityType}" ]
			        },
			        "selected-items": {
				        name: "selected-items",
				        keys: [ "selected-items" ]
			        }
		        },
		        item: {
			        "": {
				        "format": "authority",
				        "get": Alfresco.constants.PROXY_URI + "api/orgstruct/authority/?nodeRef={nodeRef}"
			        }
		        },
		        children: {
			        "root": {
				        "format": "authority",
				        "get": Alfresco.constants.PROXY_URI + "api/orgstruct/group/?root=true"
			        },
			        "search": {
				        "format": "authority",
				        "get": Alfresco.constants.PROXY_URI + "api/orgstruct/authorities/?filter={query}&user=true"
			        },
			        "selected-items": {
				        "format": "authority"
			        },
			        "GROUP": {
				        "format": "authority",
				        "get": Alfresco.constants.PROXY_URI + "api/orgstruct/group/{shortName}/children/?" + childrenQuery
			        },
			        "GROUP_EVERYONE": null,
		        },
		        titles: {
			        "root": "{title}",
			        "GROUP": "{displayName} ({shortName})",
			        "USER": "{firstName} {lastName} ({shortName})"
		        }
	        };
	        this.widgets.picker.setOptions({
		        multipleSelectMode: true,
		        model: thisModel,
		        tree: {
			        buttons: {
				        "GROUP": ["itemSelect"],
				        "USER": ["itemSelect"],
	                    "selected-yes": [ "itemUnselect" ]
                    }
	            },
	            list: {
					buttons: {
						"selected-yes": [ "itemUnselect" ]
					}
				}
			});
	        this.widgets.picker.subscribe("itemsSelected", this.onAuthoritySelected, this, true);
	        this.widgets.picker.model.subscribe("childrenUpdated", this.onChildrenUpdated, this, true);
        },

	    onChildrenUpdated: function(args) {
		    if(args.from._item_name_ == "root" || args.from._item_name_ == "search") {
			    this.widgets.picker.model.addItem({
				    authorityType: "GROUP",
				    shortName: "EVERYONE",
				    fullName: "GROUP_EVERYONE",
				    displayName: this.msg("group.everyone"),
			    }, args.from, true);
		    }
	    },

        onAuthorityFinderLoaded: function Permissions_onAuthorityFinderLoaded(response) {
            var finderDiv = Dom.get(this.id + "-authorityFinder");
            if (finderDiv) {
                finderDiv.innerHTML = response.serverResponse.responseText;

                this.widgets.authorityFinder = finderDiv;
                this.modules.authorityFinder = Alfresco.util.ComponentManager.get(this.id + "-authorityFinder");

                this.modules.authorityFinder.setOptions({
                    dataWebScript: Alfresco.constants.URL_SERVICECONTEXT + "components/people-finder/authority-query",
                    viewMode: Alfresco.AuthorityFinder.VIEW_MODE_COMPACT,
                    singleSelectMode: true,
                    minSearchTermLength: 3
                });

                this.widgets.addUserGroup = Alfresco.util.createYUIButton(this, "addUserGroupButton", this.onAddUserGroupButton, {
                    type: "checkbox",
                    checked: false
                });

                var btnRegion = Dom.getRegion(this.id + "-addUserGroupButton");
                Dom.setStyle(this.widgets.authorityFinder, "top", (btnRegion.bottom + 4) + "px");
            }
        },

        onAddUserGroupButton: function Permissions_onAddUserGroupButton(e, args) {
	        this.widgets.picker.setSelectedItems([]);
	        this.widgets.picker.show();
        },

        changePermission: function MP_changePermission(elCheckbox, checked, accessType, authority, permission) {
            var query = (checked?'add':'remove') + ';' +
                authority + ';' +
                permission + ';' +
                ('ALLOWED' == accessType) + ';';
            $.ajax({
                url: Alfresco.constants.PROXY_URI + 'api/permissions/permissions-service',
                type: 'GET',
                data: {
                    nodeRef: $html(this.options.nodeRef),
                    json: '{ "permissions": [ "' + query + '" ] }'
                },
                async: false,
                context: this,
                success: function(resp) {
                    this.updatePermissionsInfoImpl(resp);
                }
            }).fail(function(jqXHR, textStatus) {
                $.error(jqXHR);
                $.error(textStatus);
            });
        },

        removePermissions: function MP_removePermissions(authority) {
            var query = 'clear_authority' + ';' + authority;
            $.ajax({
                url: Alfresco.constants.PROXY_URI + 'api/permissions/permissions-service',
                type: 'GET',
                data: {
                    nodeRef: $html(this.options.nodeRef),
                    json: '{ "permissions": [ "' + query + '" ] }'
                },
                async: false,
                context: this,
                success: function(resp) {
                    Alfresco.util.PopupManager.displayMessage({
                        text: this.msg("delete-action-completed", authority)
                    });
                    this.updatePermissionsInfoImpl(resp);
                    this.fillAuthorityTable();
                }
            }).fail(function(jqXHR, textStatus) {
                $.error(jqXHR);
                $.error(textStatus);
            });
        },

        showHidePermissions: function MP_showHidePermissions(rowGroupClass) {
            $('.'+rowGroupClass).each(function(){
                if ('none' == $(this).css('display')) {
                    $(this).show('normal');
                } else {
                    $(this).hide('normal');
                }
            });
        },

        showHideAllPermissions: function MP_showHideAllPermissions(rowClass) {
            var isAllOpen = true;
            $('.' + rowClass).each(function(){
                if ('none' == $(this).css('display')) {
                    isAllOpen = false;
                    return;
                }
            });
            $('.' + rowClass).each(function(){
                if (isAllOpen) {
                    $(this).hide('normal');
                } else {
                    if ('none' == $(this).css('display')) {
                        $(this).show('normal');
                    }
                }
            });
        },

        updatePermissionsInfo: function MP_updatePermissionsInfo() {
            this.options.authorityPermissions = {};
            $.ajax({
                type: 'GET',
                url: Alfresco.constants.PROXY_URI + 'api/permissions/get-permissions',
                data: { nodeRef: $html(this.options.nodeRef) },
                async: false,
                context: this,
                success: function(resp) {
                    this.updatePermissionsInfoImpl(resp);
                }
            }).fail(function(jqXHR, textStatus) {
                $.error(jqXHR);
                $.error(textStatus);
            });
        },

        updatePermissionsInfoImpl: function MP_updatePermissionsInfoImpl(resp) {
            this.options.authorityPermissions = {};
            /* {
             "inherit" true,
             "permissions": [
                {
                     "authority": {
                            "avatar": "",
                            "name": "",
                            "displayName": ""
                     },
                    "access": "",
                    "permission": "",
                    "inherit": true
                },
                ...
             ]
            } */
            this.options.inherit = resp['inherit'];
            for (var i = 0; i < resp['permissions'].length; i++) {
                var p = resp['permissions'][i];
                if (!this.options.authorityPermissions[p.authority.name]) {
                    this.options.authorityPermissions[p.authority.name] = {
                        authority: p['authority'],
                        permissions: []
                    };
                }
                this.options.authorityPermissions[p.authority.name]['permissions'].push(p);
            }
        },

        createAuthorityTable: function MP_createAuthorityTable() {
            this.options.dataSource = new YAHOO.util.DataSource(this.options.availableAuthority);
            this.options.dataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;

            var authorityTableColumnDefs = [{
                key:"authorityIco",
                label: "",
                sortable:false,
                resizeable:false,
                width: 32,
                formatter: this.bind(this.renderCellAuthorityIco)
            },{
                key:"authority",
                label: this.msg("authority"),
                sortable:false,
                resizeable:true,
                formatter: this.bind(function MP_renderCellAuthority(elCell, oRecord, oColumn, oData) {
                    elCell.innerHTML = oData['displayName'];
                })
            }, {
                key:"permissions",
                label: this.msg("permissions"),
                sortable:false,
                resizeable:true,
                formatter: this.bind(this.renderCellPermissions)
            }, {
                key:"actions",
                label: this.msg("actions"),
                sortable:false,
                resizeable:true,
                formatter: this.bind(this.renderCellActions)
            }];
            this.options.authorityTable = new YAHOO.widget.DataTable(
                this.id + "-authorityTable", authorityTableColumnDefs, this.options.dataSource, {
                    selectionMode:"single",
                    renderLoopSize: 32,
                    MSG_EMPTY: this.msg("message.empty")
                }
            );
        },

        renderCellAuthorityIco: function MP_renderCellAuthorityIco(elCell, oRecord, oColumn, oData) {
            Dom.setStyle(elCell, "width", oColumn.width + "px");
            Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

            var authority = oData;
            var isGroup = authority['name'].indexOf("GROUP_") === 0, iconUrl;
	        if(isGroup) {
                iconUrl = Alfresco.constants.URL_RESCONTEXT + "components/images/group-64.png";
	        } else {
		        iconUrl = Alfresco.constants.URL_RESCONTEXT + "components/images/no-user-photo-64.png";
	        }

            if (authority['avatar'] && authority['avatar'].length !== 0) {
                iconUrl = Alfresco.constants.PROXY_URI + authority['avatar'] + "?c=queue&ph=true";
            } else if (authority.iconUrl) {
                iconUrl = authority.iconUrl;
            }
            elCell.innerHTML = '<img class="icon32" src="' + iconUrl + '" alt="icon" />';
        },

       renderCellPermissions: function MP_renderCellPermissions(elCell, oRecord, oColumn, oData) {
            var thisId = this.id;
            var rowClass = 'row-class-' + oData['rowIndex'];
            function generateCheckboxHtml(id, authority, permissionAlias, accessType) {
                var maskKey = permissionAlias + '-' + accessType;
                var mask = oData['permissionsMask'][maskKey];
                return '<input ' +
                            'id="' + id + '" ' +
                            'class="permission" ' +
                            'authority="' + authority['name'] + '" ' +
                            'permission="' + permissionAlias + '" ' +
                            'accessType="' + accessType + '" ' +
                            (true == mask['exist']? 'checked="checked" ' : '') +
                            (true == mask['inherit']? 'disabled="disabled" ' : '') +
                            'type="checkbox"/>';
            }
            var me = this;
            function linkEventListeners(allowedId, deniedId) {
                YAHOO.util.Event.addListener(allowedId, "click", function() {
                    me.onChangePermission.apply(me, [allowedId, deniedId]);
                });
                YAHOO.util.Event.addListener(deniedId, "click", function() {
                    me.onChangePermission.apply(me, [deniedId, allowedId]);
                });
            }

            // all show-hide button
            var permissionsTableHtml = 
                '<a class="show-hide-details showHideAllPermissions ' + this.id + '" href="#" rel="' + rowClass + '" name=".showHideAllPermissions"><a/>';

            // makeup table
            permissionsTableHtml += 
                '<table class="permissionsTable" style="border-width:0px;padding:5px;">' +
                    '<thead>' +
                        '<tr>' +
                            '<td style="border-width:0px;"></td>' +
                            '<td style="border-width:0px;"> '+this.msg("allow")+' &nbsp; </td>' +
                            '<td style="border-width:0px;"> '+this.msg("deny")+' &nbsp; </td>' +
                        '</tr>' +
                    '</thead>';

            // permissions group
            var gpList = this.options['supportedPermissions'];
            for (var i = 0; i < gpList.length; i++) {
                var rowGroupClass = 'permissions-' + oData['rowIndex'] + '-' + i;

                // group title
                permissionsTableHtml += 
                    '<tr class="permission-group">' +
                        '<td class="permission-name" style="border-width:0px; vertical-align:top; text-align:left; font-weight:bold;">' +
                            '<a href="#" class="' + this.id + ' showHidePermissions" rel="' + rowGroupClass + '" name=".showHidePermissions">' +
                                gpList[i]['groupBrief'] + ' &nbsp;' +
                            '</a>' +
                        '</td>' +
                        '<td class="permission-allow" style="border-width:0px;"></td>' +
                        '<td class="permission-deny" style="border-width:0px;"></td>' +
                    '</tr>';

                // permissions in group
                var pList = gpList[i]['permissions'];
                for (var j = 0; j < pList.length; j++) {
                    var allowedId = Alfresco.util.generateDomId();
                    var deniedId = Alfresco.util.generateDomId();
                    permissionsTableHtml += 
                        '<tr class="permission-item ' + rowGroupClass + ' ' + rowClass + '" style="display:none;">' +
                            '<td class="permission-name" style="border-width:0px;vertical-align:top; padding-left: 15px;">' + pList[j]['brief'] + ' &nbsp;</td>' +
                            '<td class="permission-allow" style="border-width:0px;"> ' + generateCheckboxHtml(allowedId, oData['authority'], pList[j]['alias'], 'ALLOWED') + ' </td>' +
                            '<td class="permission-deny" style="border-width:0px;vertical-align:top;"> ' + generateCheckboxHtml(deniedId, oData['authority'], pList[j]['alias'], 'DENIED') + ' </td>' +
                        '</tr>';
                    linkEventListeners(allowedId, deniedId);
                }
            }
            permissionsTableHtml += 
                '</table>';

            elCell.innerHTML = permissionsTableHtml;
        },

        onChangePermission: function MP_onChangePermission(setCheckboxId, unsetCheckboxId) {
            var setCheckbox = $('#' + setCheckboxId), unsetCheckbox = $('#' + unsetCheckboxId);
            if (setCheckbox.prop("checked")) {
                if (!unsetCheckbox.prop("disabled")) {
                    unsetCheckbox.prop("checked", false);
                }
            }
        },

        renderCellActions: function MP_renderCellActions(elCell, oRecord, oColumn, oData) {
            var buttonHtml = $('<div></div>').append(
                $('<a>', {
                    'text': this.msg("button.remove"),
                    'class': this.id + ' removePermissions',
                    'rel': oData['name'],
                    'href': '#',
                    'name': '.removePermissions',
                    'title': this.msg("delete-permissions-for-authority")
                })
            ).html();
            elCell.innerHTML = buttonHtml;
        },

        clearAuthorityTableUI: function MP_clearAuthorityTableUI() {
            this.options.authorityTable.getRecordSet().reset();
            this.options.authorityTable.render();
        },

        fillAuthorityTable: function MP_fillAuthorityTable() {
            this.clearAuthorityTableUI();
            var rowIndex = 2;
            for (var i in this.options.authorityPermissions) {
                if(!this.options.authorityPermissions.hasOwnProperty(i)) continue;
                this.insertAuthorityIntoUI(
                    this.options.authorityPermissions[i]['authority'],
                    this.options.authorityPermissions[i]['permissions'],
                    rowIndex
                );
                rowIndex++;
            }
        },

        /**
         *
         * @param authority
         * @param permission - format: { access: '', authority: '', permission: '', inherit: '' }
         */
        insertAuthorityIntoUI: function MP_insertAuthorityIntoUI(authority, permissions, rowIndex) {

            var permissionsMask = $.extend(true, {}, this.options['permissionsMask']);
            for (var i = 0; i < permissions.length; i++) {
                var maskKey = permissions[i]['permission'] + '-' + permissions[i]['access'];
                if (permissionsMask[maskKey]) { //is supported
                    permissionsMask[maskKey]['exist'] = true;
                    if (permissions[i]['inherit']) {
                        permissionsMask[maskKey]['inherit'] = true;
                    }
                }
            }

            this.options.authorityTable.addRow({
                authorityIco: authority,
                authority: authority,
                permissions: {
                    authority: authority,
                    permissions: permissions,
                    permissionsMask: permissionsMask,
                    rowIndex: rowIndex
                },
                actions: authority
            });
        },

        navigateForward: function FormManager_navigateForward() {
            if (document.referrer) {
                document.location.href = document.referrer;
            } else if (history.length > 1) {
                history.go(-1);
            } else if (this.options.defaultUrl) {
                document.location.href = this.options.defaultUrl;
            }
        },

        onInheritedButton: function Permissions_onInheritedButton(e, p_obj) {
            var _this = this;
            Alfresco.util.PopupManager.displayPrompt({
                title: this.msg("message.confirm.inheritance.title"),
                text: this.msg("message.confirm.inheritance.description"),
                noEscape: true,
                buttons: [
                    {
                        text: this.msg("button.yes"),
                        handler: function Permissions_onInheritanceButton_yes() {
                            this.destroy();
                            _this.changeInherit(!_this.options['inherit']);
                            _this._updateInheritedUI();
                        }
                    },
                    {
                        text: this.msg("button.no"),
                        handler: function Permissions_onInheritanceButton_no() {
                            this.destroy();
                        },
                        isDefault: true
                    }
                ]
            });
        },

        _updateInheritedUI: function Permissions__updateInheritedUI() {
            var inherits = this.options['inherit'];
            // Button
            Dom.removeClass(this.id + "-inheritedButtonContainer", "inherited-" + (inherits ? "off" : "on"));
            Dom.addClass(this.id + "-inheritedButtonContainer", "inherited-" + (inherits ? "on" : "off"));
        },

        changeInherit: function MP_changeInherit(inherit) {
            var query = 'set_inherit_permissions' + ';' + inherit;
            $.ajax({
                url: Alfresco.constants.PROXY_URI + 'api/permissions/permissions-service',
                type: 'GET',
                data: {
                    nodeRef: $html(this.options.nodeRef),
                    json: '{ "permissions": [ "' + query + '" ] }'
                },
                async: false,
                context: this,
                success: function(resp) {
                    this.updatePermissionsInfoImpl(resp);
                    this.fillAuthorityTable();
                }
            }).fail(function(jqXHR, textStatus) {
                $.error(jqXHR);
                $.error(textStatus);
            });
        },

        onCancelButton: function Permissions_onCancelButton(type, args) {
            this.widgets.cancelButton.set("disabled", true);
            if (document.referrer.match(/documentlibrary([?]|$)/) || document.referrer.match(/repository([?]|$)/)) {
                history.go(-1);
            } else {
                window.location.href = $siteURL(this.nodeData.type + "-details?nodeRef=" + this.nodeData.nodeRef);
            }
        },

        /**
         * piker event
         */
        onAuthoritySelected: function Permissions_onAuthoritySelected(obj) {
            // Remove authority selector popup
            this.widgets.addUserGroup.set("checked", false);
            Dom.removeClass(this.widgets.authorityFinder, "active");
            Dom.removeClass(this.id + "-inheritedContainer", "table-mask");
            Dom.removeClass(this.id + "-directContainer", "table-mask");
            //add new authority to table
	        for(var i in obj) {
                if(!obj.hasOwnProperty(i)) continue;
	            if (this.options.authorityPermissions[obj[i].fullName]) {
		            continue;
	            } else {
	                this.options.authorityPermissions[obj[i].fullName] = {
	                    authority: {
	                        name: obj[i]['fullName'],
	                        displayName: obj[i]['displayName'],
	                        isNew: true
	                    },
	                    permissions: []
	                };
	            }
	        }
	        this.fillAuthorityTable();
        }

    });

})();