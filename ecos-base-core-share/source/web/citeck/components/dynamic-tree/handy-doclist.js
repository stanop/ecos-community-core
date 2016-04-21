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
	
	var Event = YAHOO.util.Event;
	
	Citeck.widget.HandyDocumentList = function(htmlid) {
		Citeck.widget.HandyDocumentList.superclass.constructor.call(this, htmlid);
	};
	
	YAHOO.extend(Citeck.widget.HandyDocumentList, Alfresco.DocumentList, {

		defaultData: {
			items: [],
			startIndex: 0,
			totalRecords: 0,
			metadata: {
				container: "alfresco://user/home",
				custom: {},
				itemCounts: {
					documents: 0,
					folders: 0,
				},
				onlineEditing: true,
				parent: {
					nodeRef: "alfresco://user/home"
				},
				repositoryId: "",
				workingCopyLabel: "(working copy)",
			}
		},

		onReady: function() {
			Alfresco.DocumentList.prototype.onReady.call(this);
			if(this.itemsToLoad) {
				this.loadData(this.itemsToLoad);
			}
			YAHOO.Bubbling.fire("handyDoclistReady", {
				scope: this,
				eventGroup: this.id
			});
		},

		_initLiveData: function(items) {
			return YAHOO.lang.merge(this.defaultData, {
				items: items,
				totalRecords: items.length,
			});
		},
	
		_initDataSource: function(items) {
			var dataSource = new YAHOO.util.LocalDataSource(this._initLiveData(items));
			dataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
			dataSource.responseSchema = {
			   resultsList: "items",
			   metaFields:
			   {
				  paginationRecordOffset: "startIndex",
				  totalRecords: "totalRecords",
				  totalRecordsUpper : "totalRecordsUpper" // if null then totalRecords is accurate else totalRecords is lower estimate (if -1 upper estimate is unknown)
			   }
			};
			
			Alfresco.util.Ajax.jsonGet({
				url: Alfresco.constants.URL_SERVICECONTEXT + "components/documentlibrary/data/doclist/treenode/node/alfresco/user/home?max=1",
				successCallback: {
					scope: this,
					fn: function(response) {
						this.defaultData = response.json;
						this.defaultData.items = [];
						this.defaultData.totalRecords = 0;
						this.defaultData.metadata.itemCounts.documents = 0;
						this.defaultData.metadata.itemCounts.folders = 0;
						this.defaultData.metadata.parent.permissions = null;
						this.loadData(this.widgets.dataSource.liveData.items || []);
					}
				}
			});
			
			return dataSource;
		},

		_setupDataSource: function() {
		
			Citeck.widget.HandyDocumentList.superclass._setupDataSource.call(this);
			
			var ds = this.widgets.dataSource;
			this.widgets.dataSource = this._initDataSource([]);
			this.widgets.dataSource.doBeforeCallback = ds.doBeforeCallback;
			
		},

		_setDefaultDataTableErrors: function DL__setDefaultDataTableErrors(dataTable) 
		{
			if (dataTable)
				Citeck.widget.HandyDocumentList.superclass._setDefaultDataTableErrors.call(this, dataTable);
		},

		_updateDocList: function DL__updateDocList(p_obj) {
			if (this.widgets.dataSource)
				Citeck.widget.HandyDocumentList.superclass._updateDocList.call(this, p_obj);
		},

		loadData: function(items) {
			if(this.widgets.dataSource) {
				this.widgets.dataSource.liveData = this._initLiveData(items);
				this._updateDocList({});
			} else {
				this.itemsToLoad = items;
			}
		},

		// override onSimpleDetailed
		// do not update user preferences - not to influence documentlibrary
		// do not send metadataRefresh - not to issue form refresh
        onSimpleDetailed: function DL_onSimpleDetailed(e, p_obj) 
        {
            this.options.simpleView = e.newValue.index === 0;
			// do not update user preferences:
            // this.services.preferences.set(PREF_SIMPLE_VIEW, this.options.simpleView);
			
			// do not send metadataRefresh, just silently update ourselves:
			// YAHOO.Bubbling.fire("metadataRefresh");
			this._updateDocList();
			
            if (e) 
            {
                Event.preventDefault(e);
            }
        },
		
		// override fnRenderCellActions
		// eager actions rendering
		// to allow immediate display
		fnRenderCellActions: function DL_fnRenderCellActions()
		{
			var scope = this;
			if(!this.options.oldSchoolActions) {
				return Citeck.widget.HandyDocumentList.superclass.fnRenderCellActions.call(this);
			}
			
			return function DL_renderCellActions(elCell, oRecord, oColumn, oData)
			{
				var record = oRecord.getData(),
					actions = record.actions,
					html = '',
					simple = scope.options.simpleView;
				
				oColumn.width = simple ? 80 : 200;

				Dom.setStyle(elCell, "width", oColumn.width + "px");
				Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");
				Dom.addClass(elCell.parentNode, oRecord.getData("type"));

				html = '<div id="' + scope.id + '-actions-' + oRecord.getId() + '" class="action-set ' + (simple ? "simple" : "detailed") + '">';
				record.actionParams = {};
				for(var i in actions) {
                    if(!actions.hasOwnProperty(i)) continue;
					html += scope.renderAction(actions[i], record);
				}
				html += '</div>';
				elCell.innerHTML = YAHOO.lang.substitute(html, scope.getActionUrls(record));
			};
		},


	});

})();
