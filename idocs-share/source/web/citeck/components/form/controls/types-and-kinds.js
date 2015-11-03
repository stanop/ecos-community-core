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

    if(typeof Citeck == "undefined") Citeck = {};
    if(typeof Citeck.widget == "undefined") Citeck.widget = {};

    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event;

    Citeck.widget.TypesAndKinds = function(htmlId) {
        Citeck.widget.TypesAndKinds.superclass.constructor.call(this, "Citeck.widget.TypesAndKinds", htmlId);
    };

    YAHOO.extend(Citeck.widget.TypesAndKinds, Alfresco.component.Base, {

        //  DEFAULT OPTIONS
        options: {
            rootNode: null,

            preType: null,
            preKind: null,

            hidden: null,
            mandatory: null,
            blank: null,

            fixedTypeOption: null,
            fixedKindOption: null,

            recurse: false,

            onlyTypes: null,
            onlyKinds: null
        },

        onReady: function() {
            this.field = Dom.get(this.id);

            this.selects = {
                type: Dom.getElementsByClassName("type", "select", this.field)[0],
                kind: Dom.getElementsByClassName("kind", "select", this.field)[0]
            }

            this.labels = {
                type: this.selects.type.previousElementSibling,
                kind: this.selects.kind.previousElementSibling
            }

            if (this.options.hidden && (this.options.hidden == "type" || this.options.hidden == "kind")) {
                this.labels[this.options.hidden].className += " hidden";
                this.selects[this.options.hidden].className += " hidden";
            }

            Event.addListener(this.selects.type, "change", this.onChangeType, this, true);
            // Event.addListener(this.selects.kind, "change", this.onChangeKind, this, true);


            if (this.options.mandatory) {
                if (this.options.mandatory == "both" || this.options.mandatory == "type") {
                   this.selects.type.required = true;  
                }
                if (this.options.mandatory == "both" || this.options.mandatory == "kind") {
                   this.selects.kind.required = true;
                }
            }

            Alfresco.util.Ajax.jsonGet({
                url: getCategoryChildrenUrl(this.options.rootNode ? this.options.rootNode : "workspace://SpacesStore/category-document-type-root"),
                successCallback: {
                    fn: function(response) {
                        this.selects.type.items = response.json.data;

                        if (this.options.onlyTypes && this.options.onlyTypes) {
                            var onlyItems = this.options.onlyTypes,
                                existItems = this.selects.type.items,
                                newItems = [];

                            for (var i in existItems) {
                                if (onlyItems.indexOf(existItems[i].nodeRef) > -1) {
                                    newItems.push(existItems[i]);
                                }
                            }
                            this.selects.type.items = newItems;
                        }

                        setSelectOptions(this.selects.type, this.selects.type.items, this.options.preType);

                        if (this.options.fixedTypeOption) {
                            var notFindedItemFlag = true;
                            var url = getCategoryChildrenUrl(this.options.fixedTypeOption);

                            var items = this.selects.type.options;
                            for (var i=0; i>items.length; i++) {
                                if (items[i].value == this.options.fixedTypeOption) {
                                    items[i].selected = true;
                                    this.selects.type.disabled = true;
                                    notFindedItemFlag = false;
                                    break;
                                }
                            }

                            if (notFindedItemFlag) {
                                clearSelectOptions(this.selects.type, {
                                    placeholder: (this.options.blank && (this.options.blank == "both" || this.options.blank == "type") ? this.msg("types-and-kinds.select-type") : null),
                                    mandatory: (this.options.mandatory && (this.options.mandatory == "both" || this.options.mandatory == "type"))
                                });

                                var fixedOption = document.createElement('option');
                                fixedOption.selected = true;
                                fixedOption.value = this.options.fixedTypeOption;
                                fixedOption.innerHTML = "fixedOption";
                                this.selects.type.appendChild(fixedOption);
                            }
                        }

                        this.onChangeType();
                    },
                    scope: this
                },
                failureCallback: {
                    fn: function (response) {
                        console.log("failure types load")
                    },
                    scope: this
                }
            });
        },

        onChangeType: function() {
            var selectedOption = this.selects.type.options[this.selects.type.selectedIndex];

            if (selectedOption.value) {
                // TODO: check elemens in 'this'. request if it has children

                Alfresco.util.Ajax.jsonGet({
                    url: getCategoryChildrenUrl(selectedOption.value, this.options.recurse),
                    successCallback: {
                        fn: function(response) {
                            clearSelectOptions(this.selects.kind, {
                                placeholder: (this.options.blank && (this.options.blank == "both" || this.options.blank == "kind") ? this.msg("types-and-kinds.select-kind") : null),
                                mandatory: (this.options.mandatory && (this.options.mandatory == "both" || this.options.mandatory == "kind"))
                            })

                            this.selects.kind.items = response.json.data;

                            if (this.options.onlyKinds && this.options.onlyKinds) {
                                var onlyItems = this.options.onlyKinds,
                                    existItems = this.selects.kind.items,
                                    newItems = [];

                                for (var i in existItems) {
                                    if (onlyItems.indexOf(existItems[i].nodeRef) > -1) {
                                        newItems.push(existItems[i]);
                                    }
                                }
                                this.selects.kind.items = newItems;
                            }

                            setSelectOptions(this.selects.kind, this.selects.kind.items, this.options.preKind);

                            if (this.options.fixedKindOption) {
                                var items = this.selects.kind.options;
                                for (var i in items) {
                                    if (items[i].value == this.options.fixedKindOption) {
                                        items[i].selected = true;
                                        this.selects.kind.disabled = true;
                                        break;
                                    }
                                }
                            }
                        },
                        scope: this
                    },
                    failureCallback: {
                        fn: function () {
                            console.log("failure kind load")
                        },
                        scope: this
                    }
                });
            } else {
                if (this.options.blank && (this.options.blank == "both" || this.options.blank == "kind")) {
                    clearSelectOptions(this.selects.kind, {
                        placeholder: this.msg("types-and-kinds.select-kind"),
                        mandatory: this.options.mandatory && (this.options.mandatory == "both" || this.options.mandatory == "kind")
                    });
                } else {
                    clearSelectOptions(this.selects.kind);
                }

            }
        },

        onChangeKind: function() {
            console.log("onChangeKind")
        }
    });


    Citeck.widget.TypesAndKindsView = function(htmlId) {
        Citeck.widget.TypesAndKindsView.superclass.constructor.call(this, "Citeck.widget.TypesAndKindsView", htmlId);
    };

    YAHOO.extend(Citeck.widget.TypesAndKindsView, Alfresco.component.Base, {

        //  DEFAULT OPTIONS
        options: {
            preType: null,
            preKind: null,

            hidden: null
        },

        onReady: function() {
            this.field = Dom.get(this.id);

            this.viewFields = {
                type: Dom.getElementsByClassName("type", "div", this.field)[0],
                kind: Dom.getElementsByClassName("kind", "div", this.field)[0]
            }

            this.viewValueFields = {
                type: Dom.getElementsByClassName("viewmode-value", "span", this.viewFields.type)[0],
                kind: Dom.getElementsByClassName("viewmode-value", "span", this.viewFields.kind)[0]
            }

            if (this.options.hidden && (this.options.hidden == "type" || this.options.hidden == "kind")) {
                this.viewFields[this.options.hidden].className += " hidden";
            }

            if (this.options.preType) {
                Alfresco.util.Ajax.jsonGet({
                    url: getCategoryUrl(this.options.preType),
                    successCallback: {
                        fn: function(response) {
                            var result = response.json.data;
                            if (result.name) {
                                this.viewValueFields.type.innerHTML = result.name;
                            }
                        },
                        scope: this
                    },
                    failureCallback: {
                        fn: function () {
                            console.log("failure type load")
                        },
                        scope: this
                    }
                });
            }

            if (this.options.preKind) {
                Alfresco.util.Ajax.jsonGet({
                    url: getCategoryUrl(this.options.preKind),
                    successCallback: {
                        fn: function(response) {
                            console.log("success kind load")
                            var result = response.json.data;
                            if (result.name) {
                                this.viewValueFields.kind.innerHTML = result.name;
                            }
                        },
                        scope: this
                    },
                    failureCallback: {
                        fn: function () {
                            console.log("failure kind load")
                        },
                        scope: this
                    }
                });
            }
        }
    });


    //  PRIVATE FUNCTIONS
    function getCategoryChildrenUrl(nodeRef, recurse) {
        var uriTemplate = "api/citeck/category/children?nodeRef=" + encodeURI(nodeRef);
        if (recurse && recurse == "true") { uriTemplate += "&recurse=true" };
        return Alfresco.constants.PROXY_URI + uriTemplate;
    }

    function getCategoryUrl(nodeRef) {
        var uriTemplate = "api/citeck/category?nodeRef=" + encodeURI(nodeRef);
        return Alfresco.constants.PROXY_URI + uriTemplate;
    }

    function setSelectOptions(select, items, selectedItem) {
        for (var i = 0, new_option; i < items.length; i++) {
            new_option = document.createElement('option');
            new_option.value = items[i].nodeRef;

            var title = "";
            if (items[i].depth) {
                var depth = parseInt(items[i].depth);
                for (var j = 0; j < depth; j++) {
                    title += "--";
                }
                title += " ";
            }
            title += items[i].name
            new_option.text = title;

            if (selectedItem) {
                if (new_option.value == selectedItem) {
                    new_option.selected = true;
                }
            }

            select.appendChild(new_option);
        }
    }

    function clearSelectOptions(select, options) {
        options = options || {};
        select.innerHTML = "";

        if (options) {
            if (options.placeholder) {
                emptyOption = document.createElement('option');
                emptyOption.selected = true;
                emptyOption.value = "";

                if (options.mandatory) {
                    emptyOption.disabled = true;
                }

                emptyOption.innerHTML = options.placeholder;
                select.appendChild(emptyOption);
            }
        }
    }

})();
