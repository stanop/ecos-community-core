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
 * CiteckObjectFinder component.
 *
 * @namespace Alfresco
 * @class Alfresco.CiteckObjectFinder
 */
(function()
{
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom,
		Selector = YAHOO.util.Selector,
        Event = YAHOO.util.Event,
        KeyListener = YAHOO.util.KeyListener;

    /**
     * Alfresco Slingshot aliases
     */
    var $html = Alfresco.util.encodeHTML,
        $hasEventInterest = Alfresco.util.hasEventInterest,
        $combine = Alfresco.util.combinePaths,
        $siteURL = Alfresco.util.siteURL;

    /**
     * CiteckObjectFinder constructor.
     *
     * @param {String} htmlId The HTML id of the parent element
     * @param {String} currentValueHtmlId The HTML id of the parent element
     * @return {Alfresco.CiteckObjectFinder} The new CiteckObjectFinder instance
     * @constructor
     */
    Alfresco.CiteckObjectFinder = function Alfresco_CiteckObjectFinder(htmlId, currentValueHtmlId)
    {
        Alfresco.CiteckObjectFinder.superclass.constructor.call(this, "Alfresco.CiteckObjectFinder", htmlId, ["button", "menu", "container", "resize", "datasource", "datatable"]);
        this.currentValueHtmlId = currentValueHtmlId;

        /**
         * Decoupled event listeners
         */
        this.eventGroup = htmlId;
        YAHOO.Bubbling.on("renderCurrentValue", this.onRenderCurrentValue, this);
        YAHOO.Bubbling.on("selectedItemAdded", this.onSelectedItemAdded, this);
        YAHOO.Bubbling.on("selectedItemRemoved", this.onSelectedItemRemoved, this);
        YAHOO.Bubbling.on("parentChanged", this.onParentChanged, this);
        YAHOO.Bubbling.on("parentDetails", this.onParentDetails, this);
        YAHOO.Bubbling.on("formContainerDestroyed", this.onFormContainerDestroyed, this);
        YAHOO.Bubbling.on("removeListItem", this.onRemoveListItem, this);
		YAHOO.Bubbling.on("formContentReady", this.onFormContentReady, this);
		YAHOO.Bubbling.on("beforeFormRuntimeInit", this.onBeforeFormRuntimeInit, this);

        // Initialise prototype properties
        this.pickerId = htmlId + "-picker";
        this.columns = [];
        this.selectedItems = {};
		this.forms = {};
        this.isReady = false;

        this.options.objectRenderer = new Alfresco.CiteckObjectRenderer(this);

        return this;
    };

    YAHOO.extend(Alfresco.CiteckObjectFinder, Alfresco.component.Base,
        {
            /**
             * Object container for initialization options
             *
             * @property options
             * @type object
             */
            options:
            {

				/**
				 * Page mode - whether this component was loaded directly on page (true) or via ajax (false).
				 * 
				 * @property pageMode
				 * @type boolean
				 */
                pageMode: null,

				/**
				 * Current site id
				 * 
				 * @property siteId
				 * @type string
				 */
                siteId: "",
				
				/**
				 * Whether we should search the whole repository, regardless of current site.
				 * 
				 * @property searchWholeRepo
				 * @type boolean
				 */
				searchWholeRepo: false,
				
                /**
                 * Instance of an CiteckObjectRenderer class
                 *
                 * @property objectRenderer
                 * @type object
                 */
                objectRenderer: null,

                /**
                 * The selected value to be displayed (but not yet persisted)
                 *
                 * @property selectedValue
                 * @type string
                 * @default null
                 */
                selectedValue: null,

                /**
                 * The current value
                 *
                 * @property currentValue
                 * @type string
                 */
                currentValue: "",

                /**
                 * The id of the item being edited
                 *
                 * @property currentItem
                 * @type string
                 */
                currentItem: null,

                /**
                 * Value type.
                 * Whether values are passed into and out of the control as nodeRefs or other data types
                 *
                 * @property valueType
                 * @type string
                 * @default "nodeRef"
                 */
                valueType: "nodeRef",

                /**
                 * The name of the field that the object finder displays
                 *
                 * @property field
                 * @type string
                 */
                field: null,

                /**
                 * The type of the item to find
                 *
                 * @property itemType
                 * @type string
                 */
                itemType: "cm:content",

                /**
                 * The 'family' of the item to find can be one of the following:
                 *
                 * - node
                 * - category
                 * - authority
                 *
                 * default is "node".
                 *
                 * @property itemFamily
                 * @type string
                 */
                itemFamily: "node",

                /**
                 * Compact mode flag
                 *
                 * @property compactMode
                 * @type boolean
                 * @default false
                 */
                compactMode: false,

                /**
                 * Multiple Select mode flag
                 *
                 * @property multipleSelectMode
                 * @type boolean
                 * @default false
                 */
                multipleSelectMode: true,

                /**
                 * Determines whether a link to the target
                 * node should be rendered
                 *
                 * @property showLinkToTarget
                 * @type boolean
                 * @default false
                 */
                showLinkToTarget: false,

                /**
                 * Template string or function to use for link to target nodes, must
                 * be supplied when showLinkToTarget property is
                 * set to true
                 *
                 * @property targetLinkTemplate If of type string it will be used as a template, if of type function an
                 * item object will be passed as argument and link is expected to be returned by the function
                 * @type (string|function)
                 */
                targetLinkTemplate: null,

                /**
                 * Number of characters required for a search
                 *
                 * @property minSearchTermLength
                 * @type int
                 * @default 1
                 */
                minSearchTermLength: 1,

                /**
                 * Maximum number of items to display in the results list
                 *
                 * @property maxSearchResults
                 * @type int
                 * @default 100
                 */
                maxSearchResults: 100,

                /**
                 * Flag to determine whether the added and removed items
                 * should be maintained and posted separately.
                 * If set to true (the default) the picker will update
                 * a "${field.name}_added" and a "${field.name}_removed"
                 * hidden field, if set to false the picker will just
                 * update a "${field.name}" hidden field with the current
                 * value.
                 *
                 * @property maintainAddedRemovedItems
                 * @type boolean
                 * @default true
                 */
                maintainAddedRemovedItems: true,

                /**
                 * Flag to determine whether the picker is in disabled mode
                 *
                 * @property disabled
                 * @type boolean
                 * @default false
                 */
                disabled: false,

                /**
                 * Flag to indicate whether the field is mandatory
                 *
                 * @property mandatory
                 * @type boolean
                 * @default false
                 */
                mandatory: false,

                /**
                 * Relative URI of "create new item" data webscript.
                 *
                 * @property createNewItemUri
                 * @type string
                 * @default ""
                 */
                createNewItemUri: "",

                /**
                 * Icon type to augment "create new item" row.
                 *
                 * @property createNewItemIcon
                 * @type string
                 * @default ""
                 */
                createNewItemIcon: "",

                /**
                 * The display mode to use for the current values.
                 * Allowed values are "items" or "list"
                 *
                 * @property extendedMode
                 * @type string
                 * @default "items"
                 */
                displayMode: "items",

                /**
                 * The actions to display next to each item/current value in "list" mode.
                 * - if "event" has been set: A click will fire an event with name as defined by "event" and item info as attribute.
                 * - if "link" has been set: A normal html link will be displayed with href set to the value of "link"
                 * {
                 *    name: {String},  // The name of the action (used as a css class name for styling)
                 *    event: {Object}, // If present will be the name of the event to send
                 *    link: {String|function},  // If present will set the browser to display the link provided
                 *    label: {String}  // The message label key use to get the display label
                 * }
                 *
                 * @property listActions
                 * @type Array
                 * @default [ ] // Note! If allowRemoveAction equals true and
                 *                       options.disabled is false and
                 *                       displayMode equals "list"
                 *                       a remove action will be added
                 */
                listItemActions: [ ],

                /**
                 * Determines if items shall be removable in "list" display mode
                 *
                 * @property allowRemoveAction
                 * @type boolean
                 * @default true
                 */
                allowRemoveAction: true,

                /**
                 * Determines if an "Remove all" button shall be displayed in "list" display mode
                 *
                 * @property allowRemoveAllAction
                 * @type boolean
                 * @default true
                 */
                allowRemoveAllAction: true,

                /**
                 * Determines if an "Add/Select" button shall be displayed that will display an items picker
                 *
                 * @property allowSelectAction
                 * @type boolean
                 * @default true
                 */
                allowSelectAction: true,

                /**
                 * Determines if a link is rendered for content that has children, if true
                 * the content's children can be navigated.
                 *
                 * @property allowNavigationToContentChildren
                 * @type boolean
                 * @default false
                 */
                allowNavigationToContentChildren: false,

                /**
                 * The label of the select button that triggers the object finder dialog
                 *
                 * @property selectActionLabel
                 * @type string
                 */
                selectActionLabel: null,

                /**
                 * The resource id for the label of the select button that triggers the object finder dialog
                 *
                 * @property selectActionLabelId
                 * @type string
                 */
                selectActionLabelId: null,

                /**
                 * Specifies the location the object finder should start, the following
                 * values are supported:
                 *
                 * - {companyhome}
                 * - {userhome}
                 * - {siteshome}
                 * - {doclib}
                 * - {self}
                 * - {parent}
                 * - A NodeRef
                 * - An XPath
                 *
                 * @property startLocation
                 * @type string
                 */
                startLocation: null,

                /**
                 * Specifies the parameters to pass to the node locator service
                 * when determining the start location node.
                 *
                 * @property startLocationParams
                 * @type string
                 */
                startLocationParams: null,

                /**
                 * Specifies the Root Node, above which the object picker will not navigate.
                 * Values supported are:
                 *
                 * - {companyhome}
                 * - {userhome}
                 * - {siteshome}
                 * - A NodeRef
                 * - An XPath
                 */
                rootNode: null,

				onlyFiltered: false,

                /**
                 * It is a value of 'formId' parameter of the search form.
                 */
                searchFormId: 'search',
                
                /**
                 * It is a value of 'formId' parameter of the create form.
                 */
                createFormId: null,
				/**
				 * Destination folder nodeRef, where new objects should be persisted by forms (form destination).
				 * destFolder should be specified for create option to be visible.
				 */
				destFolder: null,
				
				/**
				 * Default selected mode.
				 */
				defaultMode: "picker",
				
				/**
				 * Sort by this field.
				 */
				sortField: "cm:name",

                elementsLocalization: "select-search-assoc.picker-button.label"
            },

            filterNodes: null,

            /**
             * Resizable columns
             *
             * @property columns
             * @type array
             * @default []
             */
            columns: null,

            /**
             * Single selected item, for when in single select mode
             *
             * @property singleSelectedItem
             * @type string
             */
            singleSelectedItem: null,

            currentSingleSelectedItem: null,

            /**
             * Selected items. Keeps a list of selected items for correct Add button state.
             *
             * @property selectedItems
             * @type object
             */
            selectedItems: {},

            currentSelectedItems: {},

            /**
             * Determines if this component is ready (to be called from outside)
             *
             * @property isReady
             * @type boolean
             */
            isReady: false,

            /**
             * Set multiple initialization options at once.
             *
             * @override
             * @method setOptions
             * @param obj {object} Object literal specifying a set of options
             * @return {Alfresco.CiteckObjectFinder} returns 'this' for method chaining
             */
            setOptions: function CiteckObjectFinder_setOptions(obj)
            {
                Alfresco.CiteckObjectFinder.superclass.setOptions.call(this, obj);
                // TODO: Do we need to filter this object literal before passing it on..?
                this.options.objectRenderer.setOptions(obj);

                return this;
            },

            /**
             * Set messages for this component.
             *
             * @method setMessages
             * @param obj {object} Object literal specifying a set of messages
             * @return {Alfresco.CiteckObjectFinder} returns 'this' for method chaining
             */
            setMessages: function CiteckObjectFinder_setMessages(obj)
            {
                Alfresco.CiteckObjectFinder.superclass.setMessages.call(this, obj);
                this.options.objectRenderer.setMessages(obj);
                return this;
            },

            /**
             * Populate selected items.
             *
             * @method selectItems
             * @param items {Array} Array of item ids to populate the current value with
             */
            selectItems: function CiteckObjectFinder_selectItems(items)
            {
                this.options.selectedValue = items;
                this._loadSelectedItems();
            },

            /**
             * Fired by YUI when parent element is available for scripting.
             * Component initialisation, including instantiation of YUI widgets and event listener binding.
             *
             * @method onReady
             */
            onReady: function CiteckObjectFinder_onReady()
            {
				this.widgets.modeButtons = new YAHOO.widget.ButtonGroup({
					name: this.id + "mode",
					value: "picker",
					container: this.id + "-mode-selector"
				});
                
				var filterChecked = false;
				var pickerChecked = false;
				var createChecked = false;

				if(this.options.defaultMode == 'filter') {
					filterChecked = true;
				}
				if(this.options.defaultMode == 'picker') {
					pickerChecked = true;
				}
				if(this.options.defaultMode == 'create') {
					createChecked = true;
				}

				this.widgets.modeButtons.addButtons([
					{ value: "picker", label: this.msg(this.options.elementsLocalization), checked: pickerChecked },
					{ value: "filter", label: this.msg("select-search-assoc.filter-button.label"), checked: filterChecked}
				]);
				
				if(this.options.destFolder) {
					this.widgets.modeButtons.addButtons([
						{ value: "create", label: this.msg("select-search-assoc.create-button.label"), checked: createChecked }
					]);
				}

				this.widgets.modeButtons.subscribe("valueChange", this.onModeChanged, this, true);

	            /**
	             * map definition.
	             */
	            this.options.modeMap = {
		            _data: [],

			            push: function modeMap_push(key, value) {
			            this._data.push({key: key, value: value});
		            },

		            clear: function modeMap_clear() {
			            this._data = [];
		            },

		            get: function modeMap_get(key) {
			            for (var i in this._data) {
                            if(!this._data.hasOwnProperty(i)) continue;
				            if (this._data[i].key == key) {
					            return this._data[i].value;
				            }
			            }
			            return 0;
		            }
	            }

	            //map for buttons value and index
	            var buttons = this.widgets.modeButtons.getButtons();
	            for (var i = 0; i < buttons.length; i++) {
		            this.options.modeMap.push(buttons[i]._configs.value.value, i);
	            }
				
                // if form load via ajax request,
                // we try to get siteId after load js-component from DOM document url
                if (!this.options.siteId && false == this.options.pageMode) {
                    var url = document.location.href;
                    url = url.replace(/\?.+/,''); //remove params
                    var sitePageUrlArgs = url.match(/page\/site\/.+\//g);
                    if (sitePageUrlArgs) {
                        this.options.siteId = sitePageUrlArgs[0].split('/')[2];
                    }
                }

                this._createSelectedItemsControls();
                if (!this.options.disabled)
                {
                    // Control is NOT in view mode
                    if (this.options.compactMode)
                    {
                        Dom.addClass(this.pickerId, "compact");
                    }

                    this._createNavigationControls();
                    var itemGroupActionsContainerEl = Dom.get(this.id + "-itemGroupActions");
                    if (itemGroupActionsContainerEl)
                    {
                        // Create an "Add/Select" button that will display a picker to add items
                        if (this.options.allowSelectAction)
                        {
                            var addButtonEl = document.createElement("button");
                            itemGroupActionsContainerEl.appendChild(addButtonEl);

                            var addButtonLabel = this.options.selectActionLabel;
                            if (this.options.selectActionLabelId && this.options.selectActionLabelId.length !== "")
                            {
                                addButtonLabel = this.msg(this.options.selectActionLabelId);
                            }
                            this.widgets.addButton = Alfresco.util.createYUIButton(this, null, this.onAddButtonClick,
                                {
                                    label: addButtonLabel,
                                    disabled: true
                                }, addButtonEl);
                        }
                        // Create a "Remove all" button to remove all items (if component is in "list" mode)
                        if (this.options.allowRemoveAllAction && this.options.displayMode == "list")
                        {
                            var removeAllButtonEl = document.createElement("button");
                            itemGroupActionsContainerEl.appendChild(removeAllButtonEl);
                            this.widgets.removeAllButton = Alfresco.util.createYUIButton(this, null, this.onRemoveAllButtonClick,
                                {
                                    label: this.msg("button.removeAll"),
                                    disabled: true
                                }, removeAllButtonEl);
                        }
                    }
                    if (this.options.allowRemoveAction && this.options.displayMode == "list")
                    {
                        this.options.listItemActions.push(
                            {
                                name: "remove-list-item",
                                event: "removeListItem",
                                label: "form.control.object-picker.remove-item"
                            });
                    }
                    this.widgets.ok = Alfresco.util.createYUIButton(this, "ok", this.onOK);
                    this.widgets.cancel = Alfresco.util.createYUIButton(this, "cancel", this.onCancel);

                    // force the generated buttons to have a name of "-" so it gets ignored in
                    // JSON submit. TODO: remove this when JSON submit behaviour is configurable
                    Dom.get(this.id + "-ok-button").name = "-";
                    Dom.get(this.id + "-cancel-button").name = "-";

                    this.widgets.dialog = Alfresco.util.createYUIPanel(this.pickerId, {
                        width: "800px"
                    });
                    this.widgets.dialog.hideEvent.subscribe(this.onCancel, null, this);
                    Dom.addClass(this.pickerId, "object-finder");
                }

                this._loadSelectedItems();
            },

            onSetFilter: function CiteckObjectFinder_onSetFilter() {
                this.doFilter(function() {
					this._switchMode(0);
				});
                    Dom.setStyle(this.pickerId + "-filter-mode", "display", "none");
                    Dom.setStyle(this.pickerId + "-picker-mode", "display", "block");
                
            },

            onResetFilter: function CiteckObjectFinder_onResetFilter() {
                this.filterNodes = null;
                this._refreshFilterForm();
                //this._fireRefreshEvent();
                this._switchMode(0);
            },
			
			onResetCreateForm: function() {
				this._refreshCreateForm();
			},
			
			/**
			 * Switch picker mode, between, e.g. picker, filter and create modes.
			 * @param mode - number of mode (number of button in modeButtons button-group)
			 */
			_switchMode: function CiteckObjectFinder__switchMode(mode) {
				this.widgets.modeButtons.check(mode);
			},
			
			/**
			 * Event handler - picker mode changed.
			 * Old mode is hidden, new mode is shown.
			 * Forms are initialized lazyly.
			 */
			onModeChanged: function CiteckObjectFinder_onModeChanged(e) {
				Dom.setStyle(this.pickerId + "-" + e.prevValue + "-mode", "display", "none");
				Dom.setStyle(this.pickerId + "-" + e.newValue + "-mode", "display", "block");
				if(e.newValue == 'filter') {
					if(!this.forms.filter) {
						this._refreshFilterForm();
						Dom.setStyle(this.pickerId + "-filter-mode", "display", "block");
						Dom.setStyle(this.pickerId + "-create-mode", "display", "none");
						Dom.setStyle(this.pickerId + "-picker-mode", "display", "none");
					}
				}
				if(e.newValue == 'create') {
					if(!this.forms.create) {
						this._refreshCreateForm();
						Dom.setStyle(this.pickerId + "-filter-mode", "display", "none");
						Dom.setStyle(this.pickerId + "-create-mode", "display", "block");
						Dom.setStyle(this.pickerId + "-picker-mode", "display", "none");
					}
				}
				if(e.newValue == 'picker') {
					if(!this.forms.picker) {
						this._refreshPickerForm();
						Dom.setStyle(this.pickerId + "-filter-mode", "display", "none");
						Dom.setStyle(this.pickerId + "-create-mode", "display", "none");
						Dom.setStyle(this.pickerId + "-picker-mode", "display", "block");
					}
				}
                this._dialogCentratorByScrollEvent();
			},

            _dialogCentratorByScrollEvent: function() {
                document.dispatchEvent(new window.Event("scroll", {bubbles : true, cancelable : true}));
            },
			
			/**
			 * Event handler - form initialized.
			 * Set special event handlers on buttons.
			 */
			onFormContentReady: function(layer, args) {
				// initialize filter form:
				if(args[1] == this.forms.filter) {
					with(this.forms.filter.buttons) {
						submit.setAttributes({ label: this.msg("select-search-assoc.filter-apply-button.label") });
						submit.unsubscribeAll("click");
						submit.subscribe("click", this.onSetFilter, this, true);
						reset.unsubscribeAll("click");
						reset.subscribe("click", this.onResetFilter, this, true);
                        cancel.unsubscribeAll("click");
                        cancel.subscribe("click", this.onCancel, this, true);
					}
				}
				// initialize create form:
				if(args[1] == this.forms.create) {
					with(this.forms.create.buttons) {
						reset.unsubscribeAll("click");
						reset.subscribe("click", this.onResetCreateForm, this, true);
                        cancel.unsubscribeAll("click");
                        cancel.subscribe("click", this.onCancel, this, true);
					}
				}
			},

			/**
			 * Event handler - form runtime is created.
			 * Set create form submit handlers.
			 */
			onBeforeFormRuntimeInit: function(layer, args) {
				if(this.forms.create && args[1].runtime == this.forms.create.formsRuntime) {
					this.forms.create.formsRuntime.setAJAXSubmit(true, {
						successCallback: {
							fn: this.onCreateItemSuccess,
							scope: this
						},
						failureCallback: {
							fn: this.onCreateItemFailure,
							scope: this
						}
					});
				}
			},

			/** 
			 * Event handler - create form submitted successfully.
			 * Selects created object and updates views.
			 */
			onCreateItemSuccess: function(response) {
				if(!this.options.multipleSelectMode || !this.selectedValue) {
					this.selectItems(response.json.persistedObject);
				} else {
					this.selectItems(this.selectedValue + "," + response.json.persistedObject);
				}
				this._switchMode(0);
				this.forms.create = null;
			},
			
			/**
			 * Event handler - create form submit failed.
			 * Shows error message.
			 */
			onCreateItemFailure: function(response) {
				Alfresco.util.PopupManager.displayMessage({
					text: response.json.message,
				});
			},

	        /**
	         * Refresh picker form.
	         * Tries to load 'filter' form, and if it is not defined - 'search' form.
	         */
	        _refreshPickerForm: function CiteckObjectFinder_refreshPickerForm(callback) {
		        this.options.objectRenderer._updateItems(this.options.parentNodeRef, "");
		        this.forms.picker = true;
	        },

			/**
			 * Refresh filter form.
			 * Tries to load 'filter' form, and if it is not defined - 'search' form.
			 */
            _refreshFilterForm: function CiteckObjectFinder_refreshFilterForm(callback) {
                this._refreshFormImpl(this.pickerId + "-filter-dialog-form", {
					itemKind: 'type',
					itemId: this.options.itemType,
					mode: 'edit',
					formId: this.options.searchFormId,
					showSubmitButton: 'true',
					showCancelButton: 'true',
					showResetButton: 'true',
				}, function() {
					this.forms.filter = Alfresco.util.ComponentManager.get(this.pickerId + '-filter-dialog-form-form-form');
					var htmlid = this.pickerId + "-filter-dialog-form-form";
					if (this.options.onlyFiltered) {
						this.doFilter(callback);
					} else {
						this._fireRefreshEvent();
						if(callback) callback.call(this);
					}
				});
            },

			/**
			 * Refresh create form.
			 * Tries to get 'destination' parameter first.
			 */
			_refreshCreateForm: function(callback) {
				this._refreshFormImpl(this.pickerId + "-create-dialog-form", {
					itemKind: 'type',
					itemId: this.options.itemType,
					mode: 'create',
					formId: this.options.createFormId,
					showSubmitButton: 'true',
					showCancelButton: 'true',
					showResetButton: 'true',
					destination: this.options.destFolder,
					submitType: "json"
				}, function() {
					this.forms.create = Alfresco.util.ComponentManager.get(this.pickerId + '-create-dialog-form-form-form');
					if(callback) callback.call(this);
				});
			},

			/**
			 * Refresh any form.
			 */
            _refreshFormImpl: function CiteckObjectFinder_refreshFilterForm(container, config, callback) {
                Dom.get(container).innerHTML = "<div class='form-loading'>" + this.msg('label.loading') + "</div>";
                var formUrl = YAHOO.lang.substitute(
                    Alfresco.constants.URL_SERVICECONTEXT + "components/form?itemKind={itemKind}&itemId={itemId}&mode={mode}&formId={formId}&showSubmitButton={showSubmitButton}&showCancelButton={showCancelButton}&showResetButton={showResetButton}&destination={destination}&submitType={submitType}", config
                );
				var htmlid = container + "-form";
                Alfresco.util.Ajax.request({
                    url: formUrl,
                    dataObj: {
						htmlid: htmlid
					},
                    successCallback: {
                        fn: function (response) {
                            Dom.get(container).innerHTML = response.serverResponse.responseText;
							if(callback) callback.call(this);
                            this._dialogCentratorByScrollEvent();
                        },
                        scope: this
                    },
                    failureMessage: "Could not load form component '" + formUrl + "'.",
                    scope: this,
                    execScripts: true
                });
            },

            doFilter: function CiteckObjectFinder_doFilter(callback) {
                var filterFormData = this._buildFormAjaxForSubmit(Dom.get(this.forms.filter.id));
                filterFormData.datatype = this.options.itemType;
                var searchUrl = YAHOO.lang.substitute(
                    Alfresco.constants.PROXY_URI + "slingshot/search?site={siteId}&tag=&maxResults={maxResults}&sort={sort}&query={query}&repo={repoMode}", {
                        query: encodeURIComponent(YAHOO.lang.JSON.stringify(filterFormData).replace(/assoc_/gi, 'prop_')),
                        siteId: this.options.siteId ? this.options.siteId : '',
                        repoMode: this.options.searchWholeRepo ? 'true' : 'false',
						sort: this.options.sortField,
						maxResults: this.options.maxSearchResults
                    }
                );
                YAHOO.util.Connect.asyncRequest(
                    'GET',
                    searchUrl, {
                    success: function (o) {
                        if (o.responseText) {
                                var searchResults = eval('(' + o.responseText + ')');
                                if (!searchResults['query']) {
                                Alfresco.util.PopupManager.displayMessage({
                                    text: this.msg("form.control.object-picker.filter.empty")
                                });
                                return;
                            }
                            this.filterNodes = {};
                                if (searchResults.items.length > 0) {
                                var me = this;
                                for(var i in searchResults.items) {
                                    if(!searchResults.items.hasOwnProperty(i)) continue;
                                    var nodeRef = searchResults.items[i]['nodeRef'];
                                    this.filterNodes[nodeRef] = true;
                                }
                            }
                            if(callback) callback.call(this);
                            this._fireRefreshEvent();
                        }
                    },
                    failure: function() {
                        Alfresco.logger.error("filter request error");
                    },
                    scope: this
                });
            },

            _buildFormAjaxForSubmit: function(form) {
                if (form !== null) {
                    var formData = {}, length = form.elements.length;
                    for (var i = 0; i < length; i++) {
                        var element = form.elements[i],
                            name = element.name;
                        if (name == "-" || element.disabled || element.type === "button") {
                            continue;
                        }
                        if (name == undefined || name == "") {
                            name = element.id;
                        }
                        var value = (element.type === "textarea") ? element.value : YAHOO.lang.trim(element.value);
                        if (name) {
                            // check whether the input element is an array value
                            if ((name.length > 2) && (name.substring(name.length - 2) == '[]')) {
                                name = name.substring(0, name.length - 2);
                                if (formData[name] === undefined) {
                                    formData[name] = new Array();
                                }
                                formData[name].push(value);
                            }
                            // check whether the input element is an object literal value
                            else if (name.indexOf(".") > 0) {
                                var names = name.split(".");
                                var obj = formData;
                                var index;
                                for (var j = 0, k = names.length - 1; j < k; j++) {
                                    index = names[j];
                                    if (obj[index] === undefined) {
                                        obj[index] = {};
                                    }
                                    obj = obj[index];
                                }
                                obj[names[j]] = value;
                            } else if (!((element.type === "checkbox" || element.type === "radio") && !element.checked)) {
                                if (element.type == "select-multiple") {
                                    for (var j = 0, jj = element.options.length; j < jj; j++) {
                                        if (element.options[j].selected) {
                                            if (formData[name] == undefined) {
                                                formData[name] = new Array();
                                            }
                                            formData[name].push(element.options[j].value);
                                        }
                                    }
                                } else {
                                    formData[name] = value;
                                }
                            }
                        }
                    }
                    return formData;
                }
            },

            /**
             * Destroy method - deregister Bubbling event handlers
             *
             * @method destroy
             */
            destroy: function CiteckObjectFinder_destroy()
            {
                try
                {
                    YAHOO.Bubbling.unsubscribe("renderCurrentValue", this.onRenderCurrentValue, this);
                    YAHOO.Bubbling.unsubscribe("selectedItemAdded", this.onSelectedItemAdded, this);
                    YAHOO.Bubbling.unsubscribe("selectedItemRemoved", this.onSelectedItemRemoved, this);
                    YAHOO.Bubbling.unsubscribe("parentChanged", this.onParentChanged, this);
                    YAHOO.Bubbling.unsubscribe("parentDetails", this.onParentDetails, this);
                    YAHOO.Bubbling.unsubscribe("formContainerDestroyed", this.onFormContainerDestroyed, this);
                    YAHOO.Bubbling.unsubscribe("removeListItem", this.onRemoveListItem, this);
                    YAHOO.Bubbling.unsubscribe("formContentReady", this.onFormContentReady, this);
                    YAHOO.Bubbling.unsubscribe("beforeFormRuntimeInit", this.onBeforeFormRuntimeInit, this);
				}
                catch (e)
                {
                    // Ignore
                }
                Alfresco.CiteckObjectFinder.superclass.destroy.call(this);
            },

            /**
             * Add button click handler, shows picker
             *
             * @method onAddButtonClick
             * @param e {object} DomEvent
             * @param p_obj {object} Object passed back from addListener method
             */
            onAddButtonClick: function CiteckObjectFinder_onAddButtonClick(e, p_obj) {
                this.onAddButtonClickImpl(e, p_obj);
                if (e) {
                    Event.preventDefault(e);
                }
            },

            /**
             * Add button click handler, shows picker
             *
             * @method onAddButtonClick
             * @param e {object} DomEvent
             * @param p_obj {object} Object passed back from addListener method
             */
            onAddButtonClickImpl: function CiteckObjectFinder_onAddButtonClick(e, p_obj, withoutRefresh) {
				this._switchMode(this.options.modeMap.get(this.options.defaultMode));
                this.selectedItems = Alfresco.util.deepCopy(this.currentSelectedItems);
                this.singleSelectedItem = Alfresco.util.deepCopy(this.currentSingleSelectedItem);
                if(this.options.defaultMode == 'filter') {
                    if(!this.forms.filter) {
                        this._refreshFilterForm();
                    }
                    Dom.setStyle(this.pickerId + "-picker-mode", "display", "none");
                    Dom.setStyle(this.pickerId + "-filter-mode", "display", "block");
                }

                if(this.options.defaultMode == 'create') {
                    if(!this.forms.create) {
                        this._refreshCreateForm();
                    }
                    Dom.setStyle(this.pickerId + "-picker-mode", "display", "none");
                    Dom.setStyle(this.pickerId + "-filter-mode", "display", "none");
                    Dom.setStyle(this.pickerId + "-create-mode", "display", "block");
                }

                YAHOO.Bubbling.fire("renderCurrentValue", {
                    eventGroup: this
                });

                // Register the ESC key to close the dialog
                if (!this.widgets.escapeListener)
                {
                    this.widgets.escapeListener = new KeyListener(this.pickerId,
                        {
                            keys: KeyListener.KEY.ESCAPE
                        },
                        {
                            fn: function CiteckObjectFinder_onAddButtonClick_fn(eventName, keyEvent)
                            {
                                this.onCancel();
                                Event.stopEvent(keyEvent[1]);
                            },
                            scope: this,
                            correctScope: true
                        });
                }
                this.widgets.escapeListener.enable();

                this._createResizer();
                this._populateSelectedItems();
                this.widgets.dialog.show();
                this.options.objectRenderer.onPickerShow();

                if (!withoutRefresh) {
                    if (!this.options.objectRenderer.startLocationResolved && (this.options.startLocation || this.options.rootNode)) {
                        this._resolveStartLocation();
                    } else {
                        this._fireRefreshEvent();
                    }
                }
                p_obj.set("disabled", true);


            },


            /**
             * Removes all list itesm from the current value list used in "list" display mode
             *
             * @method onRemoveAllButtonClick
             * @param e {object} DomEvent
             * @param p_obj {object} Object passed back from addListener method
             */
            onRemoveAllButtonClick: function CiteckObjectFinder_onRemoveAllButtonClick(e, p_obj)
            {
                this.widgets.currentValuesDataTable.deleteRows(0, this.widgets.currentValuesDataTable.getRecordSet().getLength());
                this.selectedItems = {};
                this.singleSelectedItem = null;
                this._adjustCurrentValues();
                if (e) {
                    Event.preventDefault(e);
                }
            },

            /**
             * Folder Up Navigate button click handler
             *
             * @method onFolderUp
             * @param e {object} DomEvent
             * @param p_obj {object} Object passed back from addListener method
             */
            onFolderUp: function CiteckObjectFinder_onFolderUp(e, p_obj)
            {
                var item = p_obj.get("value");

                YAHOO.Bubbling.fire("parentChanged",
                    {
                        eventGroup: this,
                        label: item.name,
                        nodeRef: item.nodeRef
                    });
                if (e) {
                    Event.preventDefault(e);
                }
            },

            /**
             * Create New OK button click handler
             *
             * @method onCreateNewOK
             * @param e {object} DomEvent
             * @param p_obj {object} Object passed back from addListener method
             */
            onCreateNewOK: function CiteckObjectFinder_onCreateNewOK(e, p_obj)
            {
                if (e) {
                    Event.preventDefault(e);
                }
            },

            /**
             * Create New Cancel button click handler
             *
             * @method onCreateNewCancel
             * @param e {object} DomEvent
             * @param p_obj {object} Object passed back from addListener method
             */
            onCreateNewCancel: function CiteckObjectFinder_onCreateNewCancel(e, p_obj)
            {
                if (e) {
                    Event.preventDefault(e);
                }
            },

            /**
             * Picker OK button click handler
             *
             * @method onOK
             * @param e {object} DomEvent
             * @param p_obj {object} Object passed back from addListener method
             */
            onOK: function CiteckObjectFinder_onOK(e, p_obj)
            {
                this.widgets.escapeListener.disable();
                this.widgets.dialog.hide();
                this.widgets.addButton.set("disabled", false);
                if (e) {
                    Event.preventDefault(e);
                }
                this.currentSelectedItems = Alfresco.util.deepCopy(this.selectedItems);
                this.currentSingleSelectedItem = Alfresco.util.deepCopy(this.singleSelectedItem);

                YAHOO.Bubbling.fire("renderCurrentValue", {
                    eventGroup: this
                });
            },

            /**
             * Adjust the current values, added, removed input elements according to the new selections
             * and fires event to notify form listeners about the changes.
             *
             * @method _adjustCurrentValues
             */
            _adjustCurrentValues: function CiteckObjectFinder__adjustCurrentValues()
            {
                if (!this.options.disabled)
                {
                    var addedItems = this.getAddedItems(),
                        removedItems = this.getRemovedItems(),
                        selectedItems = this.getSelectedItems();

                    if (this.options.maintainAddedRemovedItems)
                    {
                        Dom.get(this.id + "-added").value = addedItems.toString();
                        Dom.get(this.id + "-removed").value = removedItems.toString();
                    }
                    Dom.get(this.currentValueHtmlId).value = selectedItems.toString();
                    if (Alfresco.logger.isDebugEnabled())
                    {
                        Alfresco.logger.debug("Hidden field '" + this.currentValueHtmlId + "' updated to '" + selectedItems.toString() + "'");
                    }

                    // inform the forms runtime that the control value has been updated (if field is mandatory)
                    if (this.options.mandatory)
                    {
                        YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
                    }

                    YAHOO.Bubbling.fire("formValueChanged",
                        {
                            eventGroup: this,
                            addedItems: addedItems,
                            removedItems: removedItems,
                            selectedItems: selectedItems,
                            selectedItemsMetaData: Alfresco.util.deepCopy(this.selectedItems)
                        });

                    this._enableActions();
                }
            },

            /**
             * Picker Cancel button click handler
             *
             * @method onCancel
             * @param e {object} DomEvent
             * @param p_obj {object} Object passed back from addListener method
             */
            onCancel: function CiteckObjectFinder_onCancel(e, p_obj)
            {
                this.widgets.escapeListener.disable();
                this.widgets.dialog.hide();
                this.widgets.addButton.set("disabled", false);
                if (e) {
                    Event.preventDefault(e);
                }
            },

            /**
             * Triggers a search
             *
             * @method onSearch
             */
            onSearch: function CiteckObjectFinder_onSearch()
            {
                var searchTerm = Dom.get(this.pickerId + "-searchText").value;
                if (searchTerm.length < this.options.minSearchTermLength)
                {
                    // show error message
                    Alfresco.util.PopupManager.displayMessage(
                        {
                            text: this.msg("form.control.object-picker.search.enter-more", this.options.minSearchTermLength)
                        });
                }
                else
                {
                    // execute search
                    YAHOO.Bubbling.fire("refreshItemList",
                        {
                            eventGroup: this,
                            searchTerm: searchTerm
                        });
                }
            },

            /**
             * PUBLIC INTERFACE
             */

            /**
             * Returns if an item can be selected
             *
             * @method canItemBeSelected
             * @param id {string} Item id (nodeRef)
             * @return {boolean}
             */
            canItemBeSelected: function CiteckObjectFinder_canItemBeSelected(id)
            {
                if (!this.options.multipleSelectMode && this.singleSelectedItem !== null)
                {
                    return false;
                }
                return (this.selectedItems[id] === undefined);
            },

            /**
             * Returns currently selected items
             *
             * @method getSelectedItems
             * @return {array}
             */
            getSelectedItems: function CiteckObjectFinder_getSelectedItems()
            {
                var selectedItems = [];

                for (var item in this.selectedItems)
                {
                    if (this.selectedItems.hasOwnProperty(item))
                    {
                        selectedItems.push(this.selectedItems[item].nodeRef);
                    }
                }
                return selectedItems;
            },

            /**
             * Returns items that have been added to the current value
             *
             * @method getAddedItems
             * @return {array}
             */
            getAddedItems: function CiteckObjectFinder_getAddedItems()
            {
                var addedItems = [],
                    currentItems = Alfresco.util.arrayToObject(this.options.currentValue.split(","));

                for (var item in this.selectedItems)
                {
                    if (this.selectedItems.hasOwnProperty(item))
                    {
                        if (!(item in currentItems))
                        {
                            addedItems.push(item);
                        }
                    }
                }
                return addedItems;
            },

            /**
             * Returns items that have been removed from the current value
             *
             * @method getRemovedItems
             * @return {array}
             */
            getRemovedItems: function CiteckObjectFinder_getRemovedItems()
            {
                var removedItems = [],
                    currentItems = Alfresco.util.arrayToObject(this.options.currentValue.split(","));

                for (var item in currentItems)
                {
                    if (currentItems.hasOwnProperty(item))
                    {
                        if (!(item in this.selectedItems))
                        {
                            removedItems.push(item);
                        }
                    }
                }
                return removedItems;
            },


            /**
             * BUBBLING LIBRARY EVENT HANDLERS FOR PAGE EVENTS
             * Disconnected event handlers for inter-component event notification
             */

            /**
             * Renders current value in reponse to an event
             *
             * @method onRenderCurrentValue
             * @param layer {object} Event fired (unused)
             * @param args {array} Event parameters
             */
            onRenderCurrentValue: function CiteckObjectFinder_onRenderCurrentValue(layer, args)
            {
                // Check the event is directed towards this instance
                if ($hasEventInterest(this, args))
                {
                    this._adjustCurrentValues();

                    var items = this.selectedItems,
                        displayValue = "";

                    if (items === null)
                    {
                        displayValue = "<span class=\"error\">" + this.msg("form.control.object-picker.current.failure") + "</span>";
                    }
                    else
                    {
                        var item, link;
                        if (this.options.displayMode == "list")
                        {
                            var l = this.widgets.currentValuesDataTable.getRecordSet().getLength();
                            if (l > 0)
                            {
                                this.widgets.currentValuesDataTable.deleteRows(0, l);
                            }
                        }

                        for (var key in items)
                        {
                            if (items.hasOwnProperty(key))
                            {
                                item = items[key];

                                // Special case for tags, which we want to render differently to categories
                                if (item.type == "cm:category" && item.displayPath.indexOf("/categories/Tags") !== -1)
                                {
                                    item.type = "tag";
                                }

                                if (this.options.showLinkToTarget && this.options.targetLinkTemplate !== null)
                                {
                                    if (this.options.displayMode == "items")
                                    {
                                        link = null;
                                        if (YAHOO.lang.isFunction(this.options.targetLinkTemplate))
                                        {
                                            link = this.options.targetLinkTemplate.call(this, item);
                                        }
                                        else
                                        {
                                            //Discard template, build link from scratch
                                            link = $siteURL("card-details?nodeRef={nodeRef}", {
                                                nodeRef : item.nodeRef,
                                                site : item.site
                                            });
                                        }
                                        displayValue += this.options.objectRenderer.renderItem(item, 16,
                                            "<div>{icon} <a href='" + link + "'>{name}</a></div>");
                                    }
                                    else if (this.options.displayMode == "list")
                                    {
                                        this.widgets.currentValuesDataTable.addRow(item);
                                    }
                                }
                                else
                                {
                                    if (this.options.displayMode == "items")
                                    {
                                        if (item.type === "tag")
                                        {
                                            displayValue += this.options.objectRenderer.renderItem(item, null, "<div class='itemtype-tag'>{name}</div>");
                                        }
                                        else
                                        {
                                            displayValue += this.options.objectRenderer.renderItem(item, 16, "<div class='itemtype-" + $html(item.type) + "'>{icon} {name}</div>");
                                        }
                                    }
                                    else if (this.options.displayMode == "list")
                                    {
                                        this.widgets.currentValuesDataTable.addRow(item);
                                    }
                                }
                            }
                        }
                        if (this.options.displayMode == "items")
                        {
                            Dom.get(this.id + "-currentValueDisplay").innerHTML = displayValue;
                        }
                    }
                    this._enableActions();
					this._populateSelectedItems();
                }
            },

            /**
             * Selected Item Added event handler
             *
             * @method onSelectedItemAdded
             * @param layer {object} Event fired
             * @param args {array} Event parameters (depends on event type)
             */
            onSelectedItemAdded: function CiteckObjectFinder_onSelectedItemAdded(layer, args)
            {
                // Check the event is directed towards this instance
                if ($hasEventInterest(this, args))
                {
                    var obj = args[1];
                    if (obj && obj.item)
                    {
                        // Add the item at the correct position (sorted by name) in the selected list (if it hadn't been added already)
                        var records = this.widgets.dataTable.getRecordSet().getRecords(),
                            i = 0,
                            il = records.length;

                        for (; i < il; i++)
                        {
                            if (obj.item.nodeRef == records[i].getData().nodeRef)
                            {
                                break;
                            }
                        }
                        if (i == il)
                        {
                            this.widgets.dataTable.addRow(obj.item);
                            this.selectedItems[obj.item.nodeRef] = obj.item;
                            this.singleSelectedItem = obj.item;

                            if (obj.highlight)
                            {
                                // Make sure we scroll to the bottom of the list and highlight the new item
                                var dataTableEl = this.widgets.dataTable.get("element");
                                dataTableEl.scrollTop = dataTableEl.scrollHeight;
                                Alfresco.util.Anim.pulse(this.widgets.dataTable.getLastTrEl());
                            }
                        }
                        else
                        {
                            Alfresco.util.PopupManager.displayMessage(
                                {
                                    text: this.msg("message.item-already-added", $html(obj.item.name))
                                });
                        }
                    }
                }
            },

            /**
             * Selected Item Removed event handler
             *
             * @method onSelectedItemRemoved
             * @param layer {object} Event fired
             * @param args {array} Event parameters (depends on event type)
             */
            onSelectedItemRemoved: function CiteckObjectFinder_onSelectedItemRemoved(layer, args)
            {
                // Check the event is directed towards this instance
                if ($hasEventInterest(this, args))
                {
                    var obj = args[1];
                    if (obj && obj.item)
                    {
                        delete this.selectedItems[obj.item.nodeRef];
                        this.singleSelectedItem = null;
                    }
                }
            },

            /**
             * Parent changed event handler
             *
             * @method onParentChanged
             * @param layer {object} Event fired
             * @param args {array} Event parameters (depends on event type)
             */
            onParentChanged: function CiteckObjectFinder_onParentChanged(layer, args)
            {
                // Check the event is directed towards this instance
                if ($hasEventInterest(this, args))
                {
                    var obj = args[1];
                    if (obj && obj.label)
                    {
                        this.widgets.navigationMenu.set("label", '<div><span class="item-icon"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/form/images/ajax_anim.gif" width="16" height="16" alt="' + this.msg("message.please-wait") + '"></span><span class="item-name">' + $html(obj.label) + '</span></div>');
                    }
                }
            },

            /**
             * Parent Details updated event handler
             *
             * @method onParentDetails
             * @param layer {object} Event fired
             * @param args {array} Event parameters (depends on event type)
             */
            onParentDetails: function CiteckObjectFinder_onParentDetails(layer, args)
            {
                // Check the event is directed towards this instance
                if ($hasEventInterest(this, args))
                {
                    var obj = args[1];
                    if (obj && obj.parent)
                    {
                        var arrItems = [],
                            item = obj.parent,
                            navButton = this.widgets.navigationMenu,
                            navMenu = navButton.getMenu(),
                            navGroup = navMenu.getItemGroups()[0],
                            indent = "";

                        // Create array, deepest node first in final array
                        while (item)
                        {
                            arrItems = [item].concat(arrItems);
                            item = item.parent;
                        }

                        var i, ii;
                        for (i = 0, ii = navGroup.length; i < ii; i++)
                        {
                            navMenu.removeItem(0, 0, true);
                        }

                        item = arrItems[arrItems.length - 1];
                        navButton.set("label", this.options.objectRenderer.renderItem(item, 16, '<div><span class="item-icon">{icon}</span><span class="item-name">{name}</span></div>'));

                        // Navigation Up button
                        if (arrItems.length > 1)
                        {
                            this.widgets.folderUp.set("value", arrItems[arrItems.length - 2]);
                            this.widgets.folderUp.set("disabled", false);
                        }
                        else
                        {
                            this.widgets.folderUp.set("disabled", true);
                        }

                        var menuItem;
                        for (i = 0, ii = arrItems.length; i < ii; i++)
                        {
                            item = arrItems[i];
                            menuItem = new YAHOO.widget.MenuItem(this.options.objectRenderer.renderItem(item, 16, indent + '<span class="item-icon">{icon}</span><span class="item-name">{name}</span>'),
                                {
                                    value: item.nodeRef
                                });
                            menuItem.cfg.addProperty("label",
                                {
                                    value: item.name
                                });
                            navMenu.addItem(menuItem, 0);
                            indent += "&nbsp;&nbsp;&nbsp;";
                        }

                        navMenu.render();
                    }
                }
            },

            /**
             * Notification that form is being destroyed.
             *
             * @method onFormContainerDestroyed
             * @param layer {object} Event fired (unused)
             * @param args {array} Event parameters
             */
            onFormContainerDestroyed: function CiteckObjectFinder_onFormContainerDestroyed(layer, args)
            {
                if (this.widgets.dialog)
                {
                    this.widgets.dialog.destroy();
                    delete this.widgets.dialog;
                }
                if (this.widgets.resizer)
                {
                    this.widgets.resizer.destroy();
                    delete this.widgets.resizer;
                }
            },


            /**
             * Removes selected item from datatable used in "list" mode
             *
             * @method onRemoveListItem
             * @param layer {object} Event fired (unused)
             * @param args {array} Event parameters
             */
            onRemoveListItem: function CiteckObjectFinder_onRemoveListItem(event, args)
            {
                if ($hasEventInterest(this, args))
                {
                    var data = args[1].value,
                        rowId = args[1].rowId;
                    this.widgets.currentValuesDataTable.deleteRow(rowId);
                    delete this.selectedItems[data.nodeRef];
                    this.singleSelectedItem = null;
                    this._adjustCurrentValues();
                }
            },

            /**
             * Returns Icon datacell formatter
             *
             * @method fnRenderCellIcon
             */
            fnRenderCellIcon: function CiteckObjectFinder_fnRenderCellIcon()
            {
                var scope = this;

                /**
                 * Icon datacell formatter
                 *
                 * @method renderCellIcon
                 * @param elCell {object}
                 * @param oRecord {object}
                 * @param oColumn {object}
                 * @param oData {object|string}
                 */
                return function CiteckObjectFinder_renderCellIcon(elCell, oRecord, oColumn, oData)
                {
                    var iconSize = scope.options.compactMode ? 16 : 32;

                    oColumn.width = iconSize - 6;
                    Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

                    elCell.innerHTML = scope.options.objectRenderer.renderItem(oRecord.getData(), iconSize, '<div class="icon' + iconSize + '">{icon}</div>');
                };
            },

            /**
             * Returns Icon with generic width datacell formatter
             *
             * @method fnRenderCellGenericIcon
             */
            fnRenderCellGenericIcon: function CiteckObjectFinder_fnRenderCellGenericIcon()
            {
                var scope = this;

                /**
                 * Icon datacell formatter
                 *
                 * @method renderCellGenericIcon
                 * @param elCell {object}
                 * @param oRecord {object}
                 * @param oColumn {object}
                 * @param oData {object|string}
                 */
                return function CiteckObjectFinder_renderCellGenericIcon(elCell, oRecord, oColumn, oData)
                {
                    Alfresco.logger.debug("CiteckObjectFinder_renderCellGenericIcon(" + elCell + ", " + oRecord + ", " + oColumn.width + ", " + oData + ")");
                    var iconSize = scope.options.compactMode ? 16 : 32;
                    if (oColumn.width)
                    {
                        Alfresco.logger.debug("CiteckObjectFinder_renderCellGenericIcon setting width!");
                        Dom.setStyle(elCell, "width", oColumn.width + (YAHOO.lang.isNumber(oColumn.width) ? "px" : ""));
                        Dom.setStyle(elCell.parentNode, "width", oColumn.width + (YAHOO.lang.isNumber(oColumn.width) ? "px" : ""));
                    }
                    elCell.innerHTML = scope.options.objectRenderer.renderItem(oRecord.getData(), iconSize, '<div class="icon' + iconSize + '">{icon}</div>');
                };
            },

            /**
             * Returns Name / description datacell formatter
             *
             * @method fnRenderCellName
             */
            fnRenderCellName: function CiteckObjectFinder_fnRenderCellName()
            {
                var scope = this;

                /**
                 * Name / description datacell formatter
                 *
                 * @method renderCellName
                 * @param elCell {object}
                 * @param oRecord {object}
                 * @param oColumn {object}
                 * @param oData {object|string}
                 */
                return function CiteckObjectFinder_renderCellName(elCell, oRecord, oColumn, oData)
                {
                    var template;
                    if (scope.options.compactMode)
                    {
                        template = '<h3 class="name">{name}</h3>';
                    }
                    else
                    {
                        template = '<h3 class="name">{name}</h3><div class="description">{description}</div>';
                    }

                    elCell.innerHTML = scope.options.objectRenderer.renderItem(oRecord.getData(), 0, template);
                };
            },

            /**
             * Returns Remove item custom datacell formatter
             *
             * @method fnRenderCellRemove
             */
            fnRenderCellRemove: function CiteckObjectFinder_fnRenderCellRemove()
            {
                var scope = this;

                /**
                 * Remove item custom datacell formatter
                 *
                 * @method renderCellRemove
                 * @param elCell {object}
                 * @param oRecord {object}
                 * @param oColumn {object}
                 * @param oData {object|string}
                 */
                return function CiteckObjectFinder_renderCellRemove(elCell, oRecord, oColumn, oData)
                {
                    Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");
                    elCell.innerHTML = '<a href="#" class="remove-item remove-' + scope.eventGroup + '" title="' + scope.msg("form.control.object-picker.remove-item") + '" tabindex="0"><span class="removeIcon">&nbsp;</span></a>';
                };
            },


            /**
             * Returns Action item custom datacell formatter
             *
             * @method fnRenderCellListItemName
             */
            fnRenderCellListItemName: function CiteckObjectFinder_fnRenderCellListItemName()
            {
                var scope = this;

                /**
                 * Action item custom datacell formatter
                 *
                 * @method fnRenderCellListItemName
                 * @param elCell {object}
                 * @param oRecord {object}
                 * @param oColumn {object}
                 * @param oData {object|string}
                 */
                return function CiteckObjectFinder_fnRenderCellListItemName(elCell, oRecord, oColumn, oData)
                {
                    var item = oRecord.getData(),
                        description =  item.description ? $html(item.description) : scope.msg("label.none"),
                        modifiedOn = item.modified ? Alfresco.util.formatDate(Alfresco.util.fromISO8601(item.modified)) : null,
                        title = $html(item.name);
                    if (scope.options.showLinkToTarget && scope.options.targetLinkTemplate !== null)
                    {
                        var link;
                        if (YAHOO.lang.isFunction(scope.options.targetLinkTemplate))
                        {
                            link = scope.options.targetLinkTemplate.call(scope, oRecord.getData());
                        }
                        else
                        {
                            //Discard template, build link from scratch
                            link = $siteURL("card-details?nodeRef={nodeRef}", {
                                nodeRef : item.nodeRef,
                                site : item.site
                            });
                        }
                        title = '<a href="' + link + '">' + $html(item.name) + '</a>';
                    }
                    var template = '<h3 class="name">' + title + '</h3>';
                    template += '<div class="description">' + scope.msg("form.control.object-picker.description") + ': ' + description + '</div>';
                    template += '<div class="viewmode-label">' + scope.msg("form.control.object-picker.modified-on") + ': ' + (modifiedOn ? modifiedOn : scope.msg("label.none")) + '</div>';
                    elCell.innerHTML = template;
                };
            },

            fnRenderCellListItemMiddleActions: function CiteckObjectFinder_fnRenderCellListItemMiddleActions()
            {
                var scope = this;

                /**
                 * Action item custom datacell formatter
                 *
                 * @method fnRenderCellListItemActions
                 * @param elCell {object}
                 * @param oRecord {object}
                 * @param oColumn {object}
                 * @param oData {object|string}
                 */
                return function CiteckObjectFinder_fnRenderCellListItemMiddleActions(elCell, oRecord, oColumn, oData)
                {
                    if (oColumn.width)
                    {
                        Dom.setStyle(elCell, "width", oColumn.width + (YAHOO.lang.isNumber(oColumn.width) ? "px" : ""));
                        Dom.setStyle(elCell.parentNode, "width", oColumn.width + (YAHOO.lang.isNumber(oColumn.width) ? "px" : ""));
                    }

                    // While waiting for the package item actions, only render the actions (remove) in non editable mode
                    if (scope.options.disabled === false)
                    {
                        var links = "";
                        if (scope.options.nodeRef){
                            links += '<div class="list-action" onclick=\'showNewVersionDialog("'+scope.options.nodeRef+'");\' ><a href="#" tabindex="0" title="'+
                                scope.msg('ticket.upload-new-version')+'" onclick=\'showNewVersionDialog("'+scope.options.nodeRef+'");\' >'+
                                '<span><img  width=16px height=16px src="'+ Alfresco.constants.URL_RESCONTEXT +'components/documentlibrary/images/create-content-16.png" />'
                                +scope.msg('ticket.upload-new-version')+'</span></a></div>';
                        }
                        elCell.innerHTML = links;
                    }
                };
            },
            /**
             * Returns Action item custom datacell formatter
             *
             * @method fnRenderCellListItemActions
             */
            fnRenderCellListItemActions: function CiteckObjectFinder_fnRenderCellListItemActions()
            {
                var scope = this;

                /**
                 * Action item custom datacell formatter
                 *
                 * @method fnRenderCellListItemActions
                 * @param elCell {object}
                 * @param oRecord {object}
                 * @param oColumn {object}
                 * @param oData {object|string}
                 */
                return function CiteckObjectFinder_fnRenderCellListItemActions(elCell, oRecord, oColumn, oData)
                {
                    if (oColumn.width)
                    {
                        Dom.setStyle(elCell, "width", oColumn.width + (YAHOO.lang.isNumber(oColumn.width) ? "px" : ""));
                        Dom.setStyle(elCell.parentNode, "width", oColumn.width + (YAHOO.lang.isNumber(oColumn.width) ? "px" : ""));
                    }

                    // While waiting for the package item actions, only render the actions (remove) in non editable mode
                    if (scope.options.disabled === false)
                    {
                        var links = "", link, listAction;
                        for (var i = 0, il = scope.options.listItemActions.length; i < il; i++)
                        {
                            listAction = scope.options.listItemActions[i];
                            if (listAction.event)
                            {
                                links += '<div class="list-action"><a href="#" class="' + listAction.name + ' ' + ' list-action-event-' + scope.eventGroup + ' ' + listAction.event+ '" title="' + scope.msg(listAction.label) + '" tabindex="0">' + scope.msg(listAction.label) + '</a></div>';
                            }
                            else
                            {
                                link = null;
                                if (YAHOO.lang.isFunction(listAction.link))
                                {
                                    link = listAction.link.call(this, oRecord.getData());
                                }
                                else if (YAHOO.lang.isString(listAction.link))
                                {
                                    link = YAHOO.lang.substitute(listAction.link, oRecord.getData());
                                }
                                links += '<div class="list-action"><a href="' + link + '" class="' + listAction.name + '" title="' + scope.msg(listAction.label) + '" tabindex="0">' + scope.msg(listAction.label) + '</a></div>';
                            }
                        }
                        elCell.innerHTML = links;
                    }
                };
            },

            /**
             * PRIVATE FUNCTIONS
             */

            /**
             * Gets selected or current value's metadata from the repository
             *
             * @method _loadSelectedItems
             * @private
             */
            _loadSelectedItems: function CiteckObjectFinder__loadSelectedItems(useOptions)
            {
                var arrItems = "";
                if (this.options.selectedValue)
                {
                    arrItems = this.options.selectedValue;
                }
                else
                {
                    arrItems = this.options.currentValue;
                }

                var onSuccess = function CiteckObjectFinder__loadSelectedItems_onSuccess(response) {
                    var items = response.json.data.items, item;
                    this.currentSelectedItems = {};
                    //this.singleSelectedItem = null;
                    for (var i = 0, il = items.length; i < il; i++) {
                        item = items[i];
                        this.currentSelectedItems[item.nodeRef] = item;
                    }
                    this.selectedItems = Alfresco.util.deepCopy(this.currentSelectedItems);
//                    this.currentSingleSelectedItem = (this.currentSelectedItems[0])? this.currentSelectedItems[0] : null;

                    YAHOO.Bubbling.fire("renderCurrentValue", {
                        eventGroup: this
                    });
                };

                var onFailure = function CiteckObjectFinder__loadSelectedItems_onFailure(response)
                {
                    this.currentSelectedItems = null;
                };

                if (arrItems !== "")
                {
	                var itemsWithoutArchive = arrItems.split(",");
	                for (var i = 0; i <  itemsWithoutArchive.length;) {
		                if(itemsWithoutArchive[i].indexOf("archive:") !== -1) {
			                itemsWithoutArchive.splice(i, 1);
		                } else {
			                i++;
		                }
	                }
	                Alfresco.util.Ajax.jsonRequest(
                        {
                            url: Alfresco.constants.PROXY_URI + "api/forms/picker/items",
                            method: "POST",
                            dataObj:
                            {
                                items: itemsWithoutArchive,
                                itemValueType: this.options.valueType
                            },
                            successCallback:
                            {
                                fn: onSuccess,
                                scope: this
                            },
                            failureCallback:
                            {
                                fn: onFailure,
                                scope: this
                            }
                        });
                }
                else
                {
                    // if disabled show the (None) message
                    if (this.options.disabled && this.options.displayMode == "items")
                    {
                        Dom.get(this.id + "-currentValueDisplay").innerHTML = this.msg("form.control.novalue");
                    }
                    if(Dom.get(this.id)) {
                        onSuccess.call(this, { json: { data: { items: [] } } });
                    }

                    this._enableActions();
                }
            },

            /**
             * Creates the UI Navigation controls
             *
             * @method _createNavigationControls
             * @private
             */
            _createNavigationControls: function CiteckObjectFinder__createNavigationControls()
            {
                var me = this;

                if (this._inAuthorityMode())
                {
                    // only show the search box for authority mode
                    Dom.setStyle(this.pickerId + "-folderUpContainer", "display", "none");
                    Dom.setStyle(this.pickerId + "-navigatorContainer", "display", "none");
                    Dom.setStyle(this.pickerId + "-searchContainer", "display", "block");

                    // setup search widgets
                    this.widgets.searchButton = new YAHOO.widget.Button(this.pickerId + "-searchButton");
                    this.widgets.searchButton.on("click", this.onSearch, this.widgets.searchButton, this);

                    // force the generated buttons to have a name of "-" so it gets ignored in
                    // JSON submit. TODO: remove this when JSON submit behaviour is configurable
                    Dom.get(this.pickerId + "-searchButton").name = "-";

                    // register the "enter" event on the search text field
                    var zinput = Dom.get(this.pickerId + "-searchText");
                    new YAHOO.util.KeyListener(zinput,
                        {
                            keys: 13
                        },
                        {
                            fn: me.onSearch,
                            scope: this,
                            correctScope: true
                        }, "keydown").enable();
                }
                else
                {
                    // Up Navigation button
                    this.widgets.folderUp = new YAHOO.widget.Button(this.pickerId + "-folderUp",
                        {
                            disabled: true
                        });
                    this.widgets.folderUp.on("click", this.onFolderUp, this.widgets.folderUp, this);

                    // Navigation drop-down menu
                    this.widgets.navigationMenu = new YAHOO.widget.Button(this.pickerId + "-navigator",
                        {
                            type: "menu",
                            menu: this.pickerId + "-navigatorMenu",
                            lazyloadmenu: false
                        });

                    // force the generated buttons to have a name of "-" so it gets ignored in
                    // JSON submit. TODO: remove this when JSON submit behaviour is configurable
                    Dom.get(this.pickerId + "-folderUp-button").name = "-";
                    Dom.get(this.pickerId + "-navigator-button").name = "-";

                    this.widgets.navigationMenu.getMenu().subscribe("click", function (p_sType, p_aArgs)
                    {
                        var menuItem = p_aArgs[1];
                        if (menuItem)
                        {
                            YAHOO.Bubbling.fire("parentChanged",
                                {
                                    eventGroup: me,
                                    label: menuItem.cfg.getProperty("label"),
                                    nodeRef: menuItem.value
                                });
                        }
                    });

                    // Optional "Create New" UI controls
                    if (Dom.get(this.pickerId + "-createNew"))
                    {
                        // Create New - OK button
                        this.widgets.createNewOK = new YAHOO.widget.Button(this.pickerId + "-createNewOK",
                            {
                                disabled: true
                            });
                        this.widgets.createNewOK.on("click", this.onCreateNewOK, this.widgets.createNewOK, this);

                        // Create New - Cancel button
                        this.widgets.createNewCancel = new YAHOO.widget.Button(this.pickerId + "-createNewCancel",
                            {
                                disabled: true
                            });
                        this.widgets.createNewCancel.on("click", this.onCreateNewCancel, this.widgets.createNewCancel, this);
                    }
                }
            },

            /**
             * Creates UI controls to support Selected Items
             *
             * @method _createSelectedItemsControls
             * @private
             */
            _createSelectedItemsControls: function CiteckObjectFinder__createSelectedItemsControls()
            {
                var doBeforeParseDataFunction = function CiteckObjectFinder__createSelectedItemsControls_doBeforeParseData(oRequest, oFullResponse)
                {
                    var updatedResponse = oFullResponse;

                    if (oFullResponse && oFullResponse.length > 0)
                    {
                        var items = oFullResponse.data.items;

                        // Special case for tags, which we want to render differently to categories
                        var index, item;
                        for (index in items)
                        {
                            if (items.hasOwnProperty(index))
                            {
                                item = items[index];
                                if (item.type == "cm:category" && item.displayPath.indexOf("/categories/Tags") !== -1)
                                {
                                    item.type = "tag";
                                }
                            }
                        }

                        // we need to wrap the array inside a JSON object so the DataTable is happy
                        updatedResponse =
                        {
                            items: items
                        };
                    }

                    return updatedResponse;
                };

                var me = this;

                if (this.options.disabled === false)
                {

                    // Setup a DataSource for the selected items list
                    this.widgets.dataSource = new YAHOO.util.DataSource([],
                        {
                            responseType: YAHOO.util.DataSource.TYPE_JSARRAY,
                            doBeforeParseData: doBeforeParseDataFunction
                        });

                    // Picker DataTable definition
                    var columnDefinitions =
                        [
                            { key: "nodeRef", label: "Icon", sortable: false, formatter: this.fnRenderCellIcon(), width: this.options.compactMode ? 10 : 26 },
                            { key: "name", label: "Item", sortable: false, formatter: this.fnRenderCellName() },
                            { key: "remove", label: "Remove", sortable: false, formatter: this.fnRenderCellRemove(), width: 16 }
                        ];

                    this.widgets.dataTable = new YAHOO.widget.DataTable(this.pickerId + "-selectedItems", columnDefinitions, this.widgets.dataSource,
                        {
                            MSG_EMPTY: this.msg("form.control.object-picker.selected-items.empty")
                        });

                    // Hook remove item action click events
                    var fnRemoveItemHandler = function CiteckObjectFinder__createSelectedItemsControls_fnRemoveItemHandler(layer, args)
                    {
                        var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "div");
                        if (owner !== null)
                        {
                            var target, rowId, record;

                            target = args[1].target;
                            rowId = target.offsetParent;
                            record = me.widgets.dataTable.getRecord(rowId);
                            if (record)
                            {
                                me.widgets.dataTable.deleteRow(rowId);
                                YAHOO.Bubbling.fire("selectedItemRemoved",
                                    {
                                        eventGroup: me,
                                        item: record.getData()
                                    });
                            }
                        }
                        return true;
                    };
                    YAHOO.Bubbling.addDefaultAction("remove-" + this.eventGroup, fnRemoveItemHandler, true);
                }

                // Add displayMode as class so we can separate the styling of the currentValue element
                var currentValueEl = Dom.get(this.id + "-currentValueDisplay");
                Dom.addClass(currentValueEl, "object-finder-" + this.options.displayMode);

                if (this.options.displayMode == "list")
                {
                    // Setup a DataSource for the selected items list
                    var ds = new YAHOO.util.DataSource([],
                        {
                            responseType: YAHOO.util.DataSource.TYPE_JSARRAY,
                            doBeforeParseData: doBeforeParseDataFunction
                        });

                    // Current values DataTable definition
                    var currentValuesColumnDefinitions =
                        [
                            { key: "nodeRef", label: "Icon", sortable: false, formatter: this.fnRenderCellGenericIcon(), width: 50 },
                            { key: "name", label: "Item", sortable: false, formatter: this.fnRenderCellListItemName() },
                            { key: "midle_action", label: "Actions", sortable: false, formatter: this.fnRenderCellListItemMiddleActions(), width: 200 },
                            { key: "action", label: "Actions", sortable: false, formatter: this.fnRenderCellListItemActions(), width: 200 }
                        ];

                    // Make sure the currentValues container is a div rather than a span to make sure it may become a datatable
                    var currentValueId = this.id + "-currentValueDisplay";
                    currentValueEl = Dom.get(currentValueId);
                    if (currentValueEl.tagName.toLowerCase() == "span")
                    {
                        var currentValueDiv = document.createElement("div");
                        currentValueDiv.setAttribute("class", currentValueEl.getAttribute("class"));
                        currentValueEl.parentNode.appendChild(currentValueDiv);
                        currentValueEl.parentNode.removeChild(currentValueEl);
                        currentValueEl = currentValueDiv;
                    }
                    this.widgets.currentValuesDataTable = new YAHOO.widget.DataTable(currentValueEl, currentValuesColumnDefinitions, ds,
                        {
                            MSG_EMPTY: this.msg("form.control.object-picker.selected-items.empty")
                        });
                    this.widgets.currentValuesDataTable.subscribe("rowMouseoverEvent", this.widgets.currentValuesDataTable.onEventHighlightRow);
                    this.widgets.currentValuesDataTable.subscribe("rowMouseoutEvent", this.widgets.currentValuesDataTable.onEventUnhighlightRow);

                    Dom.addClass(currentValueEl, "form-element-border");
                    Dom.addClass(currentValueEl, "form-element-background-color");

                    // Hook action item click events
                    var fnActionListItemHandler = function CiteckObjectFinder__createSelectedItemsControls_fnActionListItemHandler(layer, args)
                    {
                        var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "div");
                        if (owner !== null)
                        {
                            var target, rowId, record;

                            target = args[1].target;
                            rowId = target.offsetParent;
                            record = me.widgets.currentValuesDataTable.getRecord(rowId);
                            if (record)
                            {
                                var data = record.getData(),
                                    name = YAHOO.util.Dom.getAttribute(args[1].target, "class").split(" ")[0];
                                for (var i = 0, il = me.options.listItemActions.length; i < il; i++)
                                {
                                    if (me.options.listItemActions[i].name == name)
                                    {
                                        YAHOO.Bubbling.fire(me.options.listItemActions[i].event,
                                            {
                                                eventGroup: me,
                                                value: data,
                                                rowId: rowId
                                            });
                                        return true;
                                    }
                                }
                            }
                        }
                        return true;
                    };
                    YAHOO.Bubbling.addDefaultAction("list-action-event-" + this.eventGroup, fnActionListItemHandler, true);
                }
            },

            /**
             * Populate selected items
             *
             * @method _populateSelectedItems
             * @private
             */
            _populateSelectedItems: function CiteckObjectFinder__populateSelectedItems()
            {
                if(!this.widgets.dataTable) 
                    return;
                // Empty results table
                this.widgets.dataTable.set("MSG_EMPTY", this.msg("form.control.object-picker.selected-items.empty"));
                this.widgets.dataTable.deleteRows(0, this.widgets.dataTable.getRecordSet().getLength());

                for (var item in this.selectedItems)
                {
                    if (this.selectedItems.hasOwnProperty(item))
                    {
                        YAHOO.Bubbling.fire("selectedItemAdded",
                            {
                                eventGroup: this,
                                item: this.selectedItems[item]
                            });
                    }
                }
            },

            /**
             * Resolves the start location provided to the component and refreshes
             * the picker to show the children of that start location.
             *
             * @method _resolveStartLocation
             * @private
             */
            _resolveStartLocation: function CiteckObjectFinder__resolveStartLocation()
            {
                if (this.options.startLocation || this.options.rootNode)
                {
                    this.options.startLocation = (this.options.startLocation || this.options.rootNode);

                    if (Alfresco.logger.isDebugEnabled())
                    {
                        Alfresco.logger.debug("Resolving startLocation of '" + this.options.startLocation + "'");
                    }

                    var startingNodeRef = null;

                    // check first for the start locations that don't require a remote call
                    if (this.options.startLocation.charAt(0) == "{")
                    {
                        if (this.options.startLocation == "{companyhome}")
                        {
                            startingNodeRef = "alfresco://company/home";
                        }
                        else if (this.options.startLocation == "{userhome}")
                        {
                            startingNodeRef = "alfresco://user/home";
                        }
                        else if (this.options.startLocation == "{siteshome}")
                        {
                            startingNodeRef = "alfresco://sites/home";
                        }
                        else if (this.options.startLocation == "{self}")
                        {
                            if (this.options.currentItem && this.options.currentItem !== null)
                            {
                                startingNodeRef = this.options.currentItem;
                            }
                            else
                            {
                                startingNodeRef = "alfresco://company/home";

                                if (Alfresco.logger.isDebugEnabled())
                                {
                                    Alfresco.logger.warn("To use a start location of {self} a 'currentItem' parameter is required, defaulting to company home");
                                }
                            }
                        }
                    }
                    else if (this.options.startLocation.charAt(0) == "/")
                    {
                        // start location is an XPath, this will be dealt with later so set to empty string to ignore it
                        startingNodeRef = "";
                    }
                    else
                    {
                        // start location must be a hardcoded nodeRef
                        startingNodeRef = this.options.startLocation;
                    }

                    if (startingNodeRef != null)
                    {
                        // we already know the start location so just refresh
                        this.options.objectRenderer.options.parentNodeRef = startingNodeRef;
                        this._fireRefreshEvent();
                    }
                    else
                    {
                        // we don't know the start location so try the remote node locator service
                        this._locateStartingNode();
                    }
                }
                else
                {
                    this._fireRefreshEvent();
                }
            },

            /**
             * Locates the NodeRef for the start location by calling the remote node locator
             * service and refreshes the picker.
             *
             * @method _locateStartingNode
             * @private
             */
            _locateStartingNode: function CiteckObjectFinder__locateStartingNode()
            {
                if (this.options.startLocation && this.options.currentItem && this.options.currentItem !== null)
                {
                    var nodeLocator = "companyhome";

                    // for backwards compatibility support the well known {parent} start location
                    if (this.options.startLocation == "{parent}")
                    {
                        nodeLocator = "ancestor";
                    }
                    else if (this.options.startLocation.length > 2 &&
                        this.options.startLocation.charAt(0) == "{" &&
                        this.options.startLocation.charAt(this.options.startLocation.length-1) == "}")
                    {
                        // strip off the { } characters
                        nodeLocator = this.options.startLocation.substring(1, this.options.startLocation.length-1);
                    }

                    // build the base URL for the nodelocator service call
                    var url = $combine(Alfresco.constants.PROXY_URI, "/api/", this.options.currentItem.replace("://", "/"),
                        "nodelocator", nodeLocator);

                    // add parameters for the call to the node locator service, if there are any
                    if (this.options.startLocationParams && this.options.startLocationParams != null)
                    {
                        url += "?" + encodeURI(this.options.startLocationParams);
                    }

                    // define success handler
                    var successHandler = function CiteckObjectFinder__locateStartingNode_successHandler(response)
                    {
                        var startingNodeRef = response.json.data.nodeRef;

                        if (Alfresco.logger.isDebugEnabled())
                        {
                            Alfresco.logger.debug("startLocation resolved to: " + startingNodeRef);
                        }

                        this.options.objectRenderer.options.parentNodeRef = startingNodeRef;
                        this._fireRefreshEvent();
                    };

                    // define failure handler
                    var failureHandler = function CiteckObjectFinder__locateStartingNode_failureHandler(response)
                    {
                        if (Alfresco.logger.isDebugEnabled())
                        {
                            Alfresco.logger.error("Failed to locate node: " + response.serverResponse.responseText);
                        }

                        // just use the defaults, normally company home
                        this._fireRefreshEvent();
                    };

                    if (Alfresco.logger.isDebugEnabled())
                    {
                        Alfresco.logger.debug("Generated nodelocator url: " + url);
                    }

                    // call the node locator webscript
                    var config =
                    {
                        method: "GET",
                        url: url,
                        successCallback:
                        {
                            fn: successHandler,
                            scope: this
                        },
                        failureCallback:
                        {
                            fn: failureHandler,
                            scope: this
                        }
                    };
                    Alfresco.util.Ajax.request(config);
                }
                else
                {
                    if (Alfresco.logger.isDebugEnabled())
                    {
                        Alfresco.logger.warn("To use a start location of " + this.options.startLocation +
                            " a 'currentItem' parameter is required");
                    }

                    this._fireRefreshEvent();
                }
            },

            /**
             * Fires the refreshItemList event to refresh the contents of the picker.
             *
             * @method _fireRefreshEvent
             * @private
             */
            _fireRefreshEvent: function CiteckObjectFinder__fireRefreshEvent() {
                if (this._inAuthorityMode() === false) {
                    YAHOO.Bubbling.fire("refreshItemList", {
                        eventGroup: this
                    });
                } else {
                    // get the current search term
                    var searchTermInput = Dom.get(this.pickerId + "-searchText");
                    var searchTerm = searchTermInput.value;
                    if (searchTerm.length >= this.options.minSearchTermLength) {
                        // refresh the previous search
                        YAHOO.Bubbling.fire("refreshItemList",
                            {
                                eventGroup: this,
                                searchTerm: searchTerm
                            });
                    } else {
                        // focus ready for a search
                        searchTermInput.focus();
                    }
                }
            },

            /**
             * Create YUI resizer widget
             *
             * @method _createResizer
             * @private
             */
            _createResizer: function CiteckObjectFinder__createResizer() {
                if (!this.widgets.resizer) {
                    var size = 798,
                        heightFix = 0;

                    this.columns[0] = Dom.get(this.pickerId + "-left");
                    this.columns[1] = Dom.get(this.pickerId + "-right");
                    this.widgets.resizer = new YAHOO.util.Resize(this.pickerId + "-left", {
                        handles: ["r"],
                        minWidth: 200,
                        maxWidth: (size - 200)
                    });
                    
                    // The resize handle doesn't quite get the element height correct, so it's saved here
                    heightFix = this.widgets.resizer.get("height");

                    this.widgets.resizer.on("resize", function(e) {
                        var w = e.width;
                        Dom.setStyle(this.columns[0], "height", "");
                        Dom.setStyle(this.columns[1], "width", (size - w - 8) + "px");
                    }, this, true);

                    this.widgets.resizer.on("endResize", function(e) {
                        // Reset the resize handle height to it's original value
                        this.set("height", heightFix);
                    });

                    this.widgets.resizer.fireEvent("resize", {
                        ev: 'resize',
                        target: this.widgets.resizer,
                        width: size / 2
                    });
                }
            },

            /**
             * Determines whether the picker is in 'authority' mode.
             *
             * @method _inAuthorityMode
             * @return true if the picker is being used to find authorities i.e. users and groups
             * @private
             */
            _inAuthorityMode: function CiteckObjectFinder__inAuthorityMode()
            {
                return (this.options.itemFamily == "authority");
            },


            /**
             * Determines whether the picker is in 'authority' mode.
             *
             * @method _enableActions
             * @private
             */
            _enableActions: function CiteckObjectFinder__enableActions()
            {
                if (this.widgets.removeAllButton)
                {
                    // Enable the remove all button if there is any items
                    this.widgets.removeAllButton.set("disabled", this.widgets.currentValuesDataTable.getRecordSet().getLength() === 0);
                }
                if (this.widgets.addButton)
                {
                    // Enable the add button
                    this.widgets.addButton.set("disabled", false);
                }

                if (!this.options.disabled && !this.isReady)
                {
                    this.isReady = true;
                    YAHOO.Bubbling.fire("objectFinderReady",
                        {
                            eventGroup: this
                        });
                }
            }
        });
})();


/**
 * CiteckObjectRenderer component.
 *
 * @namespace Alfresco
 * @class Alfresco.CiteckObjectRenderer
 */
(function()
{
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event,
        KeyListener = YAHOO.util.KeyListener;

    /**
     * Alfresco Slingshot aliases
     */
    var $html = Alfresco.util.encodeHTML,
        $hasEventInterest = Alfresco.util.hasEventInterest,
        $combine = Alfresco.util.combinePaths;

    /**
     * Internal constants
     */
    var IDENT_CREATE_NEW = "~CREATE~NEW~";


    /**
     * CiteckObjectRenderer constructor.
     *
     * @param {object} Instance of the CiteckObjectFinder
     * @return {Alfresco.CiteckObjectRenderer} The new CiteckObjectRenderer instance
     * @constructor
     */
    Alfresco.CiteckObjectRenderer = function(objectFinder)
    {
        this.objectFinder = objectFinder;

        Alfresco.CiteckObjectRenderer.superclass.constructor.call(this, "Alfresco.CiteckObjectRenderer", objectFinder.pickerId, ["button", "menu", "container", "datasource", "datatable"]);
        /**
         * Decoupled event listeners
         */
        this.eventGroup = objectFinder.eventGroup;
        YAHOO.Bubbling.on("refreshItemList", this.onRefreshItemList, this);
        YAHOO.Bubbling.on("parentChanged", this.onParentChanged, this);
        YAHOO.Bubbling.on("selectedItemAdded", this.onSelectedItemChanged, this);
        YAHOO.Bubbling.on("selectedItemRemoved", this.onSelectedItemChanged, this);

        // Initialise prototype properties
        this.addItemButtons = {};
        this.startLocationResolved = false;
        this.createNewItemId = null;

        return this;
    };

    YAHOO.extend(Alfresco.CiteckObjectRenderer, Alfresco.component.Base,
        {
            /**
             * Object container for initialization options
             *
             * @property options
             * @type object
             */
            options:
            {
				/**
				 * Page mode - whether this component was loaded directly on page (true) or via ajax (false).
				 * 
				 * @property pageMode
				 * @type boolean
				 */
                pageMode: null,

				/**
				 * Current site id
				 * 
				 * @property siteId
				 * @type string
				 */
                siteId: "",
				
				/**
				 * Whether we should search the whole repository, regardless of current site.
				 * 
				 * @property searchWholeRepo
				 * @type boolean
				 */
				searchWholeRepo: false,
				
                /**
                 * Parent node for browsing
                 *
                 * @property parentNodeRef
                 * @type string
                 */
                parentNodeRef: "",

                /**
                 * The type of the item to find
                 *
                 * @property itemType
                 * @type string
                 */
                itemType: "cm:content",

                /**
                 * The 'family' of the item to find can be one of the following:
                 *
                 * - node
                 * - category
                 * - authority
                 *
                 * default is "node".
                 *
                 * @property itemFamily
                 * @type string
                 */
                itemFamily: "node",

                /**
                 * Parameters to be passed to the data webscript
                 *
                 * @property params
                 * @type string
                 */
                params: "",

                /**
                 * Compact mode flag
                 *
                 * @property compactMode
                 * @type boolean
                 * @default false
                 */
                compactMode: false,

                /**
                 * Maximum number of items to display in the results list
                 *
                 * @property maxSearchResults
                 * @type int
                 * @default 100
                 */
                maxSearchResults: 100,

                /**
                 * Relative URI of "create new item" data webscript.
                 *
                 * @property createNewItemUri
                 * @type string
                 * @default ""
                 */
                createNewItemUri: "",

                /**
                 * Icon type to augment "create new item" row.
                 *
                 * @property createNewItemIcon
                 * @type string
                 * @default ""
                 */
                createNewItemIcon: ""
            },

            /**
             * Object container for storing button instances, indexed by item id.
             *
             * @property addItemButtons
             * @type object
             */
            addItemButtons: null,

            /**
             * Create new item input control Dom Id
             *
             * @property createNewItemId
             * @type string
             */
            createNewItemId: null,

            /**
             * Flag to indicate whether the start location (if present)
             * has been resolved yet or not
             *
             * @property startLocationResolved
             * @type boolean
             */
            startLocationResolved: false,


            /**
             * Fired by YUI when parent element is available for scripting.
             * Component initialisation, including instantiation of YUI widgets and event listener binding.
             *
             * @method onReady
             */
            onReady: function CiteckObjectRenderer_onReady()
            {
                // if form load via ajax request,
                // we try to get siteId after load js-component from DOM document url
                if (!this.options.siteId && false == this.options.pageMode) {
                    var url = document.location.href;
                    url = url.replace(/\?.+/,''); //remove params
                    var sitePageUrlArgs = url.match(/page\/site\/.+\//g);
                    if (sitePageUrlArgs) {
                        this.options.siteId = sitePageUrlArgs[0].split('/')[2];
                    }
                }
                this._createControls();
            },

            /**
             * Destroy method - deregister Bubbling event handlers
             *
             * @method destroy
             */
            destroy: function CiteckObjectRenderer_destroy()
            {
                try
                {
                    YAHOO.Bubbling.unsubscribe("refreshItemList", this.onRefreshItemList, this);
                    YAHOO.Bubbling.unsubscribe("parentChanged", this.onParentChanged, this);
                    YAHOO.Bubbling.unsubscribe("selectedItemAdded", this.onSelectedItemChanged, this);
                    YAHOO.Bubbling.unsubscribe("selectedItemRemoved", this.onSelectedItemChanged, this);
                }
                catch (e)
                {
                    // Ignore
                }
                Alfresco.CiteckObjectRenderer.superclass.destroy.call(this);
            },


            /**
             * PUBLIC INTERFACE
             */

            /**
             * The picker has just been shown
             *
             * @method onPickerShow
             */
            onPickerShow: function CiteckObjectRenderer_onPickerShow()
            {
                this.addItemButtons = {};
//              Dom.get(this.objectFinder.pickerId).focus();
            },

            /**
             * Generate item icon URL
             *
             * @method getIconURL
             * @param item {object} Item object literal
             * @param size {number} Icon size (16, 32)
             */
            getIconURL: function CiteckObjectRenderer_getIconURL(item, size)
            {
                return Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util.getFileIcon(item.name, item.type, size);
            },

            /**
             * Render item using a passed-in template
             *
             * @method renderItem
             * @param item {object} Item object literal
             * @param iconSize {number} Icon size (16, 32)
             * @param template {string} String with "{parameter}" style placeholders
             */
            renderItem: function CiteckObjectRenderer_renderItem(item, iconSize, template)
            {
                var me = this;

                var renderHelper = function CiteckObjectRenderer_renderItem_renderHelper(p_key, p_value, p_metadata)
                {
                    if (p_key.toLowerCase() == "icon")
                    {
                        return '<img src="' + me.getIconURL(item, iconSize) + '" width="' + iconSize + '" alt="' + $html(item.description) + '" title="' + $html(item.name) + '" />';
                    }
                    return $html(p_value);
                };

                return YAHOO.lang.substitute(template, item, renderHelper);
            },


            /**
             * BUBBLING LIBRARY EVENT HANDLERS FOR PAGE EVENTS
             * Disconnected event handlers for inter-component event notification
             */

            /**
             * Refresh item list event handler
             *
             * @method onRefreshItemList
             * @param layer {object} Event fired
             * @param args {array} Event parameters (depends on event type)
             */
            onRefreshItemList: function CiteckObjectRenderer_onRefreshItemList(layer, args)
            {
                // Check the event is directed towards this instance
                if ($hasEventInterest(this, args))
                {
                    var searchTerm = "";
                    var obj = args[1];
                    if (obj && obj.searchTerm)
                    {
                        searchTerm = obj.searchTerm;
                    }
                    this._updateItems(this.options.parentNodeRef, searchTerm);
                }
            },

            /**
             * Parent changed event handler
             *
             * @method onParentChanged
             * @param layer {object} Event fired
             * @param args {array} Event parameters (depends on event type)
             */
            onParentChanged: function CiteckObjectRenderer_onParentChanged(layer, args)
            {
                // Check the event is directed towards this instance
                if ($hasEventInterest(this, args))
                {
                    var obj = args[1];
                    if (obj && obj.nodeRef)
                    {
                        this._updateItems(obj.nodeRef, "");
                    }
                }
            },

            /**
             * Selected Item Changed event handler
             * Handles selectedItemAdded and selectedItemRemoved events
             *
             * @method onSelectedItemChanged
             * @param layer {object} Event fired
             * @param args {array} Event parameters (depends on event type)
             */
            onSelectedItemChanged: function CiteckObjectRenderer_onSelectedItemChanged(layer, args)
            {
                // Check the event is directed towards this instance
                if ($hasEventInterest(this, args))
                {
                    var obj = args[1];
                    if (obj && obj.item)
                    {
                        var button;
                        for (var id in this.addItemButtons)
                        {
                            if (this.addItemButtons.hasOwnProperty(id))
                            {
                                button = this.addItemButtons[id];
                                Dom.setStyle(button, "display", this.objectFinder.canItemBeSelected(id) ? "inline" : "none");
                            }
                        }
                    }
                }
            },

            /**
             * Returns Icon datacell formatter
             *
             * @method fnRenderItemIcon
             */
            fnRenderItemIcon: function CiteckObjectRenderer_fnRenderItemIcon()
            {
                var scope = this;

                /**
                 * Icon datacell formatter
                 *
                 * @method renderItemIcon
                 * @param elCell {object}
                 * @param oRecord {object}
                 * @param oColumn {object}
                 * @param oData {object|string}
                 */
                return function CiteckObjectRenderer_renderItemIcon(elCell, oRecord, oColumn, oData)
                {
                    var iconSize = scope.options.compactMode ? 16 : 32;

                    oColumn.width = iconSize - 6;
                    Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

                    // Create New item cell type
                    if (oRecord.getData("type") == IDENT_CREATE_NEW)
                    {
                        Dom.addClass(this.getTrEl(elCell), "create-new-row");
                        var obj =
                        {
                            type: scope.options.createNewItemIcon,
                            description: scope.msg("form.control.object-picker.create-new")
                        };
                        elCell.innerHTML = scope.renderItem(obj, iconSize, '<div class="icon' + iconSize + '"><span class="new-item-overlay"></span>{icon}</div>');
                        return;
                    }

                    elCell.innerHTML = scope.renderItem(oRecord.getData(), iconSize, '<div class="icon' + iconSize + '">{icon}</div>');
                };
            },

            /**
             * Returns Name datacell formatter
             *
             * @method fnRenderItemName
             */
            fnRenderItemName: function CiteckObjectRenderer_fnRenderItemName()
            {
                var scope = this;

                /**
                 * Name datacell formatter
                 *
                 * @method renderItemName
                 * @param elCell {object}
                 * @param oRecord {object}
                 * @param oColumn {object}
                 * @param oData {object|string}
                 */
                return function CiteckObjectRenderer_renderItemName(elCell, oRecord, oColumn, oData)
                {
                    var template = '';

                    // Create New item cell type
                    if (oRecord.getData("type") == IDENT_CREATE_NEW)
                    {
                        scope.createNewItemId = Alfresco.util.generateDomId();
                        elCell.innerHTML = '<input id="' + scope.createNewItemId + '" type="text" class="create-new-input" tabindex="0" />';
                        return;
                    }

                    if (oRecord.getData("isContainer") ||
                        (!oRecord.getData("isContainer") && (scope.options.allowNavigationToContentChildren || oRecord.getData("type") == "cm:category")))
                    {
                        template += '<h3 class="item-name"><a href="#" class="theme-color-1 parent-' + scope.eventGroup + '">{name}</a></h3>';
                    }
                    else
                    {
                        template += '<h3 class="item-name">{name}</h3>';
                    }

                    if (!scope.options.compactMode)
                    {
                        template += '<div class="description">{description}</div>';
                    }

                    elCell.innerHTML = scope.renderItem(oRecord.getData(), 0, template);
                };
            },

            /**
             * Returns Add button datacell formatter
             *
             * @method fnRenderCellAdd
             */
            fnRenderCellAdd: function CiteckObjectRenderer_fnRenderCellAdd()
            {
                var scope = this;

                /**
                 * Add button datacell formatter
                 *
                 * @method renderCellAvatar
                 * @param elCell {object}
                 * @param oRecord {object}
                 * @param oColumn {object}
                 * @param oData {object|string}
                 */
                return function CiteckObjectRenderer_renderCellAdd(elCell, oRecord, oColumn, oData)
                {
                    Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

                    var containerId = Alfresco.util.generateDomId(),
                        button;

                    // Create New item cell type
                    if (oRecord.getData("type") == IDENT_CREATE_NEW)
                    {
                        elCell.innerHTML = '<a href="#" class="create-new-item create-new-item-' + scope.eventGroup + '" title="' + scope.msg("form.control.object-picker.create-new") + '" tabindex="0"><span class="createNewIcon">&nbsp;</span></a>';
                        return;
                    }

                    if (oRecord.getData("selectable"))
                    {
                        var nodeRef = oRecord.getData("nodeRef"),
                            style = "";

                        if (!scope.objectFinder.canItemBeSelected(nodeRef))
                        {
                            style = 'style="display: none"';
                        }

                        elCell.innerHTML = '<a id="' + containerId + '" href="#" ' + style + ' class="add-item add-' + scope.eventGroup + '" title="' + scope.msg("form.control.object-picker.add-item") + '" tabindex="0"><span class="addIcon">&nbsp;</span></a>';
                        scope.addItemButtons[nodeRef] = containerId;
                    }
                };
            },

            /**
             * Create New Item button click handler
             *
             * @method onCreateNewItem
             */
            onCreateNewItem: function CiteckObjectRenderer_onCreateNewItem()
            {
                var elInput = Dom.get(this.createNewItemId),
                    uri = $combine("/", this.options.createNewItemUri).substring(1),
                    itemName;

                if (elInput)
                {
                    itemName = elInput.value;
                    if (itemName === null || itemName.length < 1)
                    {
                        return;
                    }
                    /**
                     * TODO: Validation?!
                     */
                    Alfresco.util.Ajax.jsonPost(
                        {
                            url: Alfresco.constants.PROXY_URI + uri,
                            dataObj:
                            {
                                name: itemName
                            },
                            successCallback:
                            {
                                fn: function CiteckObjectRenderer_onCreateNewItem_successCallback(p_obj)
                                {
                                    var response = p_obj.json;
                                    if (response && response.nodeRef)
                                    {
                                        var item =
                                        {
                                            type: this.options.itemType,
                                            name: response.name,
                                            nodeRef: response.nodeRef,
                                            selectable: true
                                        };

                                        // Special case for tags, which we want to render differently to categories
                                        if (item.type == "cm:category" && response.displayPath.indexOf("/categories/Tags") !== -1)
                                        {
                                            item.type = "tag";
                                        }

                                        if (!response.itemExists)
                                        {
                                            // Item didn't exist - display success message
                                            Alfresco.util.PopupManager.displayMessage(
                                                {
                                                    text: this.msg("form.control.object-picker.create-new.success", response.name)
                                                });
                                            // Add the new item to the DataTable
                                            this.widgets.dataTable.addRow(item);
                                        }

                                        // Automatically select the new item
                                        YAHOO.Bubbling.fire("selectedItemAdded",
                                            {
                                                eventGroup: this,
                                                item: item,
                                                highlight: true
                                            });
                                    }
                                    elInput.value = "";
                                },
                                scope: this
                            },
                            failureMessage: this.msg("form.control.object-picker.create-new.failure")
                        });
                }
            },

            /**
             * PRIVATE FUNCTIONS
             */

            /**
             * Creates UI controls
             *
             * @method _createControls
             */
            _createControls: function CiteckObjectRenderer__createControls()
            {
                var me = this;

                // DataSource definition
                var pickerChildrenUrl = Alfresco.constants.PROXY_URI +
                    "api/forms/picker-search/" + (this.options.siteId && !this.options.searchWholeRepo ? this.options.siteId : "undefined") +
                    "/" + this.options.itemFamily;
//                var pickerChildrenUrl = Alfresco.constants.PROXY_URI + "api/forms/picker-search/" + this.options.itemFamily;
                this.widgets.dataSource = new YAHOO.util.DataSource(pickerChildrenUrl,
                    {
                        responseType: YAHOO.util.DataSource.TYPE_JSON,
                        connXhrMode: "queueRequests",
                        responseSchema:
                        {
                            resultsList: "items",
                            metaFields:
                            {
                                parent: "parent"
                            }
                        }
                    });

                this.widgets.dataSource.doBeforeParseData = function CiteckObjectRenderer_doBeforeParseData(oRequest, oFullResponse)
                {
                    var updatedResponse = oFullResponse;

                    if (oFullResponse)
                    {
                        var items = oFullResponse.data.items;

                        // Crop item list to max length if required
                        if (me.options.maxSearchResults > -1 && items.length > me.options.maxSearchResults)
                        {
                            items = items.slice(0, me.options.maxSearchResults-1);
                        }

                        // Add the special "Create new" record if required
                        if (me.options.createNewItemUri !== "" && me.createNewItemId === null)
                        {
                            items = [{ type: IDENT_CREATE_NEW }].concat(items);
                        }

                        // Special case for tags, which we want to render differently to categories
                        var index, item;
                        for (index in items)
                        {
                            if (items.hasOwnProperty(index))
                            {
                                item = items[index];
                                if (item.type == "cm:category" && item.displayPath.indexOf("/categories/Tags") !== -1)
                                {
                                    item.type = "tag";
                                    // Also set the parent type to display the drop-down correctly. This may need revising for future type support.
                                    oFullResponse.data.parent.type = "tag";
                                }
                            }
                        }

                        // Notify interested parties of the parent details
                        YAHOO.Bubbling.fire("parentDetails",
                            {
                                eventGroup: me,
                                parent: oFullResponse.data.parent
                            });

                        // we need to wrap the array inside a JSON object so the DataTable is happy
                        updatedResponse =
                        {
                            parent: oFullResponse.data.parent,
                            items: items
                        };
                    }

                    return updatedResponse;
                };

                // DataTable column defintions
                var columnDefinitions =
                    [
                        { key: "nodeRef", label: "Icon", sortable: false, formatter: this.fnRenderItemIcon(), width: this.options.compactMode ? 10 : 26 },
                        { key: "name", label: "Item", sortable: false, formatter: this.fnRenderItemName() },
                        { key: "add", label: "Add", sortable: false, formatter: this.fnRenderCellAdd(), width: 16 }
                    ];

                var initialMessage = this.msg("form.control.object-picker.items-list.loading");
                if (this._inAuthorityMode())
                {
                    initialMessage = this.msg("form.control.object-picker.items-list.search");
                }

                this.widgets.dataTable = new YAHOO.widget.DataTable(this.id + "-results", columnDefinitions, this.widgets.dataSource,
                    {
                        renderLoopSize: 100,
                        initialLoad: false,
                        MSG_EMPTY: initialMessage
                    });

                // Rendering complete event handler
                this.widgets.dataTable.subscribe("renderEvent", function()
                {
                    if (this.options.createNewItemUri !== "")
                    {
                        if (!this.widgets.enterListener)
                        {
                            this.widgets.enterListener = new KeyListener(this.createNewItemId,
                                {
                                    keys: KeyListener.KEY.ENTER
                                },
                                {
                                    fn: function CiteckObjectRenderer__createControls_fn(eventName, keyEvent, obj)
                                    {
                                        // Clear any previous autocomplete timeout
                                        if (this.autocompleteDelayId != -1)
                                        {
                                            window.clearTimeout(this.autocompleteDelayId);
                                        }
                                        this.onCreateNewItem();
                                        Event.stopEvent(keyEvent[1]);
                                        return false;
                                    },
                                    scope: this,
                                    correctScope: true
                                }, YAHOO.env.ua.ie > 0 ? KeyListener.KEYDOWN : "keypress");
                            this.widgets.enterListener.enable();
                        }

                        me.autocompleteDelayId = -1;
                        Event.addListener(this.createNewItemId, "keyup", function(p_event)
                        {
                            var sQuery = this.value;

                            // Filter out keys that don't trigger queries
                            if (!Alfresco.util.isAutocompleteIgnoreKey(p_event.keyCode))
                            {
                                // Clear previous timeout
                                if (me.autocompleteDelayId != -1)
                                {
                                    window.clearTimeout(me.autocompleteDelayId);
                                }
                                // Set new timeout
                                me.autocompleteDelayId = window.setTimeout(function()
                                {
                                    YAHOO.Bubbling.fire("refreshItemList",
                                        {
                                            eventGroup: me,
                                            searchTerm: sQuery
                                        });
                                }, 500);
                            }
                        });

                        Dom.get(this.createNewItemId).focus();
                    }
                }, this, true);

                // Hook add item action click events (for Compact mode)
                var fnAddItemHandler = function CiteckObjectRenderer__createControls_fnAddItemHandler(layer, args)
                {
                    var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "div");
                    if (owner !== null)
                    {
                        var target, rowId, record;

                        target = args[1].target;
                        rowId = target.offsetParent;
                        record = me.widgets.dataTable.getRecord(rowId);
                        if (record)
                        {
                            YAHOO.Bubbling.fire("selectedItemAdded",
                                {
                                    eventGroup: me,
                                    item: record.getData(),
                                    highlight: true
                                });
                        }
                    }
                    return true;
                };
                YAHOO.Bubbling.addDefaultAction("add-" + this.eventGroup, fnAddItemHandler, true);

                // Hook create new item action click events (for Compact mode)
                var fnCreateNewItemHandler = function CiteckObjectRenderer__createControls_fnCreateNewItemHandler(layer, args)
                {
                    var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "div");
                    if (owner !== null)
                    {
                        me.onCreateNewItem();
                    }
                    return true;
                };
                YAHOO.Bubbling.addDefaultAction("create-new-item-" + this.eventGroup, fnCreateNewItemHandler, true);

                // Hook navigation action click events
                var fnNavigationHandler = function CiteckObjectRenderer__createControls_fnNavigationHandler(layer, args)
                {
                    var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "div");
                    if (owner !== null)
                    {
                        var target, rowId, record;

                        target = args[1].target;
                        rowId = target.offsetParent;
                        record = me.widgets.dataTable.getRecord(rowId);
                        if (record)
                        {
                            YAHOO.Bubbling.fire("parentChanged",
                                {
                                    eventGroup: me,
                                    label: record.getData("name"),
                                    nodeRef: record.getData("nodeRef")
                                });
                        }
                    }
                    return true;
                };
                YAHOO.Bubbling.addDefaultAction("parent-" + this.eventGroup, fnNavigationHandler, true);
            },

            /**
             * Updates item list by calling data webscript
             *
             * @method _updateItems
             * @param nodeRef {string} Parent nodeRef
             * @param searchTerm {string} Search term
             */
            _updateItems: function CiteckObjectRenderer__updateItems(nodeRef, searchTerm)
            {
				if(this.objectFinder.filterNodes != null) {
					this._updateItems2();
					return;
				}
				
         // Empty results table - leave tag entry if it's been rendered
         if (this.createNewItemId !== null)
         {
            this.widgets.dataTable.deleteRows(1, this.widgets.dataTable.getRecordSet().getLength() - 1);
         }
         else
         {
            this.widgets.dataTable.set("MSG_EMPTY", this.msg("form.control.object-picker.items-list.loading"));
            this.widgets.dataTable.deleteRows(0, this.widgets.dataTable.getRecordSet().getLength());
         }
         
         var successHandler = function ObjectRenderer__updateItems_successHandler(sRequest, oResponse, oPayload)
         {
            this.options.parentNodeRef = oResponse.meta.parent ? oResponse.meta.parent.nodeRef : nodeRef;
            this.widgets.dataTable.set("MSG_EMPTY", this.msg("form.control.object-picker.items-list.empty"));
            if (this.createNewItemId !== null)
            {
               this.widgets.dataTable.onDataReturnAppendRows.call(this.widgets.dataTable, sRequest, oResponse, oPayload);
            }
            else
            {
               this.widgets.dataTable.onDataReturnInitializeTable.call(this.widgets.dataTable, sRequest, oResponse, oPayload);
            }
         };
         
         var failureHandler = function ObjectRenderer__updateItems_failureHandler(sRequest, oResponse)
         {
            if (oResponse.status == 401)
            {
               // Our session has likely timed-out, so refresh to offer the login page
               window.location.reload();
            }
            else
            {
               try
               {
                  var response = YAHOO.lang.JSON.parse(oResponse.responseText);
                  this.widgets.dataTable.set("MSG_ERROR", response.message);
                  this.widgets.dataTable.showTableMessage(response.message, YAHOO.widget.DataTable.CLASS_ERROR);
               }
               catch(e)
               {
               }
            }
         };
         
         // build the url to call the pickerchildren data webscript
         var url = this._generatePickerChildrenUrlPath(nodeRef) + this._generatePickerChildrenUrlParams(searchTerm);
         
         if (Alfresco.logger.isDebugEnabled())
         {
            Alfresco.logger.debug("Generated pickerchildren url fragment: " + url);
         }
         
         // call the pickerchildren data webscript
         this.widgets.dataSource.sendRequest(url,
         {
            success: successHandler,
            failure: failureHandler,
            scope: this
         });
         
         // the start location is now resolved
         this.startLocationResolved = true;
      },
	  
			// update items after filter - when nodeRefs are known
			_updateItems2: function() {

         var successHandler = function ObjectRenderer__updateItems_successHandler(oResponse)
         {
			var items = oResponse.results = oResponse.json.data.items;
			// we trust that they all are selectable
			for(var i in items) {
                if(!items.hasOwnProperty(i)) continue;
				items[i].selectable = true;
			}
			var sRequest = null,
				oPayload = null;
            this.widgets.dataTable.set("MSG_EMPTY", this.msg("form.control.object-picker.items-list.empty"));
            if (this.createNewItemId !== null)
            {
               this.widgets.dataTable.onDataReturnAppendRows.call(this.widgets.dataTable, sRequest, oResponse, oPayload);
            }
            else
            {
               this.widgets.dataTable.onDataReturnInitializeTable.call(this.widgets.dataTable, sRequest, oResponse, oPayload);
            }
         };
         
         var failureHandler = function ObjectRenderer__updateItems_failureHandler(oResponse)
         {
            if (oResponse.status == 401)
            {
               // Our session has likely timed-out, so refresh to offer the login page
               window.location.reload();
            }
            else
            {
               try
               {
                  var response = YAHOO.lang.JSON.parse(oResponse.responseText);
                  this.widgets.dataTable.set("MSG_ERROR", response.message);
                  this.widgets.dataTable.showTableMessage(response.message, YAHOO.widget.DataTable.CLASS_ERROR);
               }
               catch(e)
               {
               }
            }
         };
				
				var items = [];
				for(var nodeRef in this.objectFinder.filterNodes) {
                    if(!this.objectFinder.filterNodes.hasOwnProperty(nodeRef)) continue;
					if(!nodeRef.match(/^archive/)) {
						items.push(nodeRef);
					}
				}
				
 				Alfresco.util.Ajax.jsonRequest(
				{
					url: Alfresco.constants.PROXY_URI + "api/forms/picker/items",
					method: "POST",
					dataObj:
					{
						items: items,
						itemValueType: this.options.valueType
					},
					successCallback:
					{
						fn: successHandler,
						scope: this
					},
					failureCallback:
					{
						fn: failureHandler,
						scope: this
					}
				});
							
			},

            /**
             * Determines whether the picker is in 'authority' mode.
             *
             * @method _inAuthorityMode
             * @return true if the picker is being used to find authorities i.e. users and groups
             */
            _inAuthorityMode: function CiteckObjectRenderer__inAuthorityMode()
            {
                return (this.options.itemFamily == "authority");
            },

            /**
             * Generates the path fragment of the pickerchildren webscript URL.
             *
             * @method _generatePickerChildrenUrlPath
             * @param nodeRef NodeRef of the parent
             * @return The generated URL
             */
            _generatePickerChildrenUrlPath: function CiteckObjectRenderer__generatePickerChildrenUrlPath(nodeRef)
            {
                // generate the path portion of the url
                if(nodeRef)
                    return $combine("/", nodeRef.replace("://", "/"), "children");
                return;
            },

            /**
             * Generates the query parameters for the pickerchildren webscript URL.
             *
             * @method _generatePickerChildrenUrlParams
             * @param searchTerm The search term
             * @return The generated URL
             */
            _generatePickerChildrenUrlParams: function CiteckObjectRenderer__generatePickerChildrenUrlParams(searchTerm)
            {
                var params = "?selectableType=" + this.options.itemType + "&searchTerm=" + encodeURIComponent(searchTerm) +
                    "&size=" + this.options.maxSearchResults;

                // if an XPath start location has been provided and it has not been resolved
                // yet, pass it to the pickerchildren script as a parameter
                if (!this.startLocationResolved && this.options.startLocation &&
                    this.options.startLocation.charAt(0) == "/")
                {
                    params += "&xpath=" + encodeURIComponent(this.options.startLocation);
                }

                // has a rootNode been specified?
                if (this.options.rootNode)
                {
                    var rootNode = null;

                    if (this.options.rootNode.charAt(0) == "{")
                    {
                        if (this.options.rootNode == "{companyhome}")
                        {
                            rootNode = "alfresco://company/home";
                        }
                        else if (this.options.rootNode == "{userhome}")
                        {
                            rootNode = "alfresco://user/home";
                        }
                        else if (this.options.rootNode == "{siteshome}")
                        {
                            rootNode = "alfresco://sites/home";
                        }
                    }
                    else
                    {
                        // rootNode is either an xPath expression or a nodeRef
                        rootNode = this.options.rootNode;
                    }
                    if (rootNode !== null)
                    {
                        params += "&rootNode=" + encodeURIComponent(rootNode);
                    }
                }

                if (this.options.params)
                {
                    params += "&" + encodeURI(this.options.params);
                }

                return params;
            }
        });
})();
