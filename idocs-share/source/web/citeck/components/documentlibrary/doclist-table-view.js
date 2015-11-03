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

if(typeof(Citeck) == "undefined") Citeck = {};
if(typeof(Citeck.doclib) == "undefined") Citeck.doclib = {};

var Dom = YAHOO.util.Dom;

Citeck.doclib.TableViewRenderer = function(name)
{
	Citeck.doclib.TableViewRenderer.superclass.constructor.call(this, name);
	this.metadataBannerViewName = "table";
	this.metadataLineViewName = "table";
	return this;
};

YAHOO.extend(Citeck.doclib.TableViewRenderer, Alfresco.DocumentListSimpleViewRenderer, {

	renderView: function DL_VR_renderView(scope, sRequest, oResponse, oPayload)
	{
		var parentId = scope.id + this.parentElementIdSuffix;
		Dom.setStyle(parentId, 'display', '');
		Dom.addClass(parentId, 'doclist-table-view');
		var hasRows = typeof oResponse.results !== 'undefined' && oResponse.results.length > 0;
		if (hasRows)
			this.updateDefaultHeaders(scope);
		else
			this.hideHeader(parentId);
		if (hasRows) {
			var template = oResponse.results[0].metadataTemplate;
			var headers = this.parseByTemplate(scope, null, template);
			if (headers.length > 0) {
				var thumbnailColumn = scope.widgets.dataTable.getColumn("thumbnail");
				if (thumbnailColumn)
					scope.widgets.dataTable.hideColumn("thumbnail");
				var fileNameColumn = scope.widgets.dataTable.getColumn("fileName");
				if (fileNameColumn)
					scope.widgets.dataTable.hideColumn("fileName");
				for (var i = 0; i < headers.length; i++) {
					var k = headers[i];
					var key = k.key;
					var label = scope.msg(k.meta ? k.meta : "doclist.header." + key);
					var c = scope.widgets.dataTable.getColumn(key);
					if (!c) {
						var oColumn = new YAHOO.widget.Column({key: key, label: label, sortable: false, formatter: this.fnRenderCell(scope)});
						scope.widgets.dataTable.insertColumn(oColumn, fileNameColumn.getIndex() + i);
					}
				}
			}
		}
		scope.widgets.dataTable.onDataReturnInitializeTable.call(scope.widgets.dataTable, sRequest, oResponse, oPayload);
	},

	updateDefaultHeaders: function(scope) {
		this.updateHeader(scope, "nodeRef");
		this.updateHeader(scope, "status");
		this.updateHeader(scope, "thumbnail");
		this.updateHeader(scope, "fileName");
		this.updateHeader(scope, "actions");
	},

	hideHeader: function(parentId) {
		var thead = Selector.query('thead', parentId);
		Dom.addClass(thead, 'hidden');
	},
	
	updateHeader: function(scope, columnName, hasRows) {
		var column = scope.widgets.dataTable.getColumn(columnName);
		if (column)
			column.label = scope.msg("doclist.header." + columnName);
	},

	/**
	 * Returns custom datacell formatter
	 *
	 * @method fnRenderCell
	 */
	fnRenderCell: function DL_fnRenderCell(scope)
	{
		/**
		 * Custom datacell formatter
		 *
		 * @method renderCell
		 * @param elCell {object}
		 * @param oRecord {object}
		 * @param oColumn {object}
		 * @param oData {object|string}
		*/
		return function DL_renderCell(elCell, oRecord, oColumn, oData)
		{
			if (typeof scope.viewRenderers[scope.options.viewRendererName] === "object")
			{
				scope.viewRenderers[scope.options.viewRendererName].renderCell(scope, elCell, oRecord, oColumn, oData);
			}
		};
	},
	
	destroyView: function DL_VR_destroyView(scope, sRequest, oResponse, oPayload)
	{
		var parentId = scope.id + this.parentElementIdSuffix;
		Dom.setStyle(parentId, 'display', 'none');
		Dom.removeClass(parentId, 'doclist-table-view');
	},

	renderCellThumbnail: function(scope, elCell, oRecord, oColumn, oData) {
		elCell.innerHTML = "";
	},
	
	renderCellDescription: function(scope, elCell, oRecord, oColumn, oData) {
		elCell.innerHTML = "";
	},

	parseByTemplate: function(scope, record, template) {
		var result = [];
		if(template && YAHOO.lang.isArray(template.lines)) {
			// We are going to split templates with several things: {key1 meta1}{key2 meta2}
			var regexp = /(\{.*?\})+?/g;
			for(var i = 0; i < template.lines.length; i++) {
				var t = template.lines[i].template;
				var keyMetas = [];
				var match = regexp.exec(t);
				while (match != null) {
					keyMetas.push(match[0]);
					match = regexp.exec(t);
				}
				for (var j = 0; j < keyMetas.length; j++) {
					YAHOO.lang.substitute(keyMetas[j], scope.renderers, function(key, value, meta) {
						var html = "";
						if(record && record.jsNode && typeof value == "function") {
							html = value.call(scope, record, "");
						} else if(record && record.jsNode && record.jsNode.properties && record.jsNode.properties[key]) {
							html = scope.renderProperty(record.jsNode.properties[key]);
						}
						result.push({'key': key, 'meta': meta, 'html': html});
						return '';
					});
				}
			}
		}
		return result;
	},
	
	getValueByKey: function(key, values) {
		var result = null;
		if(key && YAHOO.lang.isArray(values)) {
			for(var i = 0; i < values.length; i++) {
				if (key === values[i].key) {
					result = values[i].html;
					break;
				}
			}
		}
		return result;
	},
	
	renderCell: function(scope, elCell, oRecord, oColumn, oData) {
		var result = '';
		var columnKey = oColumn.getKey(),
			record = oRecord.getData(),
			template = record.metadataTemplate;
		if(columnKey != null) {
			var values = this.parseByTemplate(scope, record, template);
			var value = this.getValueByKey(columnKey, values);
			if (value)
				result = value;
		}
		elCell.innerHTML = result;
	}
});
	
// register view renderer
YAHOO.Bubbling.subscribe("postSetupViewRenderers", function(layer, args) {
	var scope = args[1].scope;
	var renderer = new Citeck.doclib.TableViewRenderer("table");
	scope.registerViewRenderer(renderer);
});

})();
