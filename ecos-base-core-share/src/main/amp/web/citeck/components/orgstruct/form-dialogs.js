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

	var Dom = YAHOO.util.Dom;
	
    Citeck.widget.SafeDialog = function(htmlid) {
        Citeck.widget.SafeDialog.superclass.constructor.call(this, htmlid);
    };
    
    YAHOO.extend(Citeck.widget.SafeDialog, Alfresco.module.SimpleDialog, {
        
        onBeforeFormRuntimeInit: function(layer, args) {
            if(args[1].runtime.formId == this.id + "-form") {
                Citeck.widget.SafeDialog.superclass.onBeforeFormRuntimeInit.apply(this, arguments);
            }
         },
         
         _hideDialog: function() {
             var destroyOnHide = this.options.destroyOnHide;
             this.options.destroyOnHide = false;
             Citeck.widget.SafeDialog.superclass._hideDialog.apply(this, arguments);
             this.options.destroyOnHide = destroyOnHide;
             
             if(destroyOnHide) {
                 this.destroy();
             }
         },
         
         destroy: function() {
             this._updateWidgetsSet();
             Citeck.widget.SafeDialog.superclass.destroy.apply(this, arguments);
             YAHOO.Bubbling.unsubscribe("beforeFormRuntimeInit", this.onBeforeFormRuntimeInit, this);

             delete this.dialog;
             delete this.widgets;
             if (this.isFormOwner)
             {
                delete this.form;
             }
         },
         
         _updateWidgetsSet: function() {
             var allWidgets = Alfresco.util.ComponentManager.list();
             _.each(allWidgets, function(widget) {
                 if(widget.id.substring(0, this.id.length) == this.id && widget.id.length > this.id.length) {
                     this.widgets[widget.id] = widget;
                 }
             }, this);
         },
         
    });

	Citeck.widget.FormDialog = function(htmlid, name) {
		Citeck.widget.FormDialog.superclass.constructor.call(this, name || "Citeck.widget.FormDialog", htmlid);
		this.widgets.panel = new Citeck.widget.SafeDialog(htmlid);
	};
	
	YAHOO.extend(Citeck.widget.FormDialog, Alfresco.component.Base);
	YAHOO.lang.augmentObject(Citeck.widget.FormDialog.prototype, YAHOO.util.EventProvider.prototype);
	YAHOO.lang.augmentObject(Citeck.widget.FormDialog.prototype, Citeck.util.ErrorManager.prototype);
	YAHOO.lang.augmentObject(Citeck.widget.FormDialog.prototype, {
	
		_show: function(config) {
			var templateUrl = YAHOO.lang.substitute(Alfresco.constants.URL_SERVICECONTEXT + "components/form?itemKind={itemKind}&itemId={itemId}&destination={destination}&mode={mode}&submitType={submitType}&formId={formId}&assocType={assocType}&showCancelButton=true",
				config, null, false
			);
			var width = (typeof this.options !== 'undefined' && typeof this.options.width !== 'undefined') ? this.options.width : "40em";
			var doBeforeDialogShow = (typeof this.options !== 'undefined' && typeof this.options.doBeforeDialogShow !== 'undefined') ? this.options.doBeforeDialogShow : {
				scope: this,
				// set title:
				fn: function(form, dialog) {
					Alfresco.util.populateHTML([dialog.dialog.id + "_h", config.title]);
				}
			}; 
			this.widgets.panel.setOptions({
				width: width,
				templateUrl: templateUrl,
				actionUrl: null,
				doBeforeDialogShow: doBeforeDialogShow,
                destroyOnHide: true, // support using this dialog on other forms
				onSuccess: config.onSuccess,
				onFailure: config.onFailure,
			}).show();
		},
	
		hide: function() {
			this.widgets.panel.hide();
		},
	
	});

	Citeck.widget.CreateFormDialog = function(htmlid, itemId, formId, destination, assocType, marker, name) {
		Citeck.widget.CreateFormDialog.superclass.constructor.call(this, htmlid, name);

		this.itemId = itemId;
		this.formId = formId;
		this.destination = destination;
		this.assocType = assocType;
		this.marker = marker;
		this.createEvent("itemCreated");
	};
	
	YAHOO.extend(Citeck.widget.CreateFormDialog, Citeck.widget.FormDialog);
	YAHOO.lang.augmentObject(Citeck.widget.CreateFormDialog.prototype, {
	
		show: function() {
			this._show({
				itemKind: "type",
				itemId: this.itemId,
				formId: this.formId,
				mode: "create",
				destination: this.destination,
				assocType: this.assocType,
				submitType: "json",
				title: this.msg("panel.create." + (this.formId||"default") + ".header"),
				onSuccess: {
					scope: this,
					fn: function(e) {
						this.fireEvent("itemCreated", this.marker !== null ? this.marker : e);
					},
				},
				onFailure: { 
					scope: this,
					fn: this.onFailure,
				},
			});
		},
		
	});
	
	Citeck.widget.EditFormDialog = function(htmlid, itemId, formId, marker, name) {
		Citeck.widget.EditFormDialog.superclass.constructor.call(this, htmlid, name);
		this.itemId = itemId;
		this.formId = formId;
		this.marker = marker;
		this.createEvent("itemEdited");
	};
	
	YAHOO.extend(Citeck.widget.EditFormDialog, Citeck.widget.FormDialog);
	YAHOO.lang.augmentObject(Citeck.widget.EditFormDialog.prototype, {

		show: function() {
			this._show({
				itemKind: "node",
				itemId: this.itemId,
				formId: this.formId,
				mode: "edit",
				submitType: "json",
				title: this.msg("panel.edit." + (this.formId||"default") + ".header"),
				onSuccess: { 
					scope: this,
					fn: function(e) {
						this.fireEvent("itemEdited", this.marker);
					},
				},
				onFailure: { 
					scope: this,
					fn: this.onFailure,
				},
			});
		},
		
	});
	
})();