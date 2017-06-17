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
    /*global YAHOO, document */
    var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Lang = YAHOO.lang, DT = YAHOO.widget.DataTable;

    var GroupedDataTable = function(elContainer, aColumnDefs, oDataSource, oConfigs) {

        // If there is no 'groupBy' attribute in oConfigs return a plain YUI DataTable
        if (!oConfigs.groupBy) {
            return new YAHOO.widget.DataTable(elContainer, aColumnDefs, oDataSource, oConfigs);
        }

        // Set message for rows with no group
        this.MSG_NOGROUP = oConfigs.MSG_NOGROUP ? oConfigs.MSG_NOGROUP : "(none)";

        // If there is an existing row formatter, save it so I can call it after my own
        this._oldFormatRow = oConfigs.formatRow;

        // Now, set my own
        oConfigs.formatRow = this.rowFormatter;

        // you can use a varable before the declaration is finished?
        GroupedDataTable.superclass.constructor.call(this, elContainer, aColumnDefs, oDataSource, oConfigs);

        this.initGroups(); // Not required but prevents flickering

        // Re-initialise the groups when data is changed
        this.subscribe("sortedByChange", function() { this.initGroups(); }); // Not required but prevents flickering
        this.subscribe("renderEvent", function() { this.initGroups(); });

        // Unselect any group when a row is clicked
        this.subscribe("rowClickEvent", function(args) { this.unselectGroup(args); });
    };

    YAHOO.widget.GroupedDataTable = GroupedDataTable;
    YAHOO.lang.extend(GroupedDataTable, YAHOO.widget.DataTable, {
        /**
         * The current group name. Used to determine when a new group starts when rowFormatter is called.
         * @property currentGroupName
         * @type {String}
         * @private
         */
        currentGroupName: [],

        /**
         * The groups found in the current data set.
         * @property groups
         * @type {Array}
         * @private
         */
        groups: [],

        /**
         * A flag to reset the group array. Set each time a new data set is passed.
         * @property resetGroups
         * @type {Boolean}
         * @private
         */
        resetGroups: true,

        /**
         * Event handler for group click.
         * @property groupClickEvent
         * @type {Event}
         */
        onGroupClick: new YAHOO.util.CustomEvent("onGroupClick", this),

        /**
         * The currently selected group
         * @property groupClickEvent
         * @type {Event}
         */
        selectedGroup: null,

        /**
         * The list group
         * @property groupClickEvent
         * @type {Event}
         */
        groupsBy: null,

        /**
         * A YUI DataTable custom row formatter. The row formatter must be applied to the DataTable
         * via the formatRow configuration property.
         * @method rowFormatter
         * @param tr {Object} To row to be formatted.
         * @param record {Object} To current data record.
         */
        rowFormatter: function(tr, record) {
            if (this.resetGroups) {
                this.groups = [];
                this.currentGroupName = [];
                this.resetGroups = false;
            }

            this.groupsBy = this.configs.groupBy;
            var lastIndex = this.groupsBy.length-1;
            var groupJson = [];
            for(var i=0; i<=lastIndex; i++) {
                if(this.currentGroupName[i]!= record.getData(this.groupsBy[i].id)) {
                    groupJson.unshift({
                        'id' : this.groupsBy[i].id,
                        'label' : YAHOO.lang.substitute(this.groupsBy[i].label, {}, function(key, value) {
                            return record.getData(key);
                        }, false)
                    });
                    for(var j=lastIndex; j>=i; j--) {
                        i!=j ? this.currentGroupName[j] = null : this.currentGroupName[j] = record.getData(this.groupsBy[j].id);
                    }
                }
            }
            this.groups.push({name: groupJson, row: tr, record: record, group: null });
            Dom.addClass(tr, "group-first-row");

            return true;
        },

        /**
         * Initialises the groups for the current data set.
         * @method initGroups
         * @private
         */
        initGroups: function() {
            if (!this.resetGroups) {
                // Insert each group in the array
                for (var i = 0; i < this.groups.length; i++) {
                    for (var j = this.groups[i].name.length-1; j >=0 ; j--) {
                        this.groups[i].group = this.insertGroup(
                            this.groups[i].name[j].label, //Group display label
                            this.groups[i].name[j].id,    //Groupped field
                            this.groups[i].row); //Nesting
                    }
                }

                this.resetGroups = true;
            }
        },

        /**
         * Inserts a group before the specified row.
         * @method insertGroup
         * @param name {String} The name of the group.
         * @param beforeRow {Object} To row to insert the group.
         * @private
         */
        insertGroup: function(name, groupId, row) {
            var index = this.getRecordIndex(row);
            var group = document.createElement("tr");
            var groupCell = document.createElement("td");
            var numberOfColumns = this.getColumnSet().keys.length;
            var icon = document.createElement("div");
            var nesting = 0;
            for(var i=0; i<this.groupsBy.length; i++) {
                if(groupId == this.groupsBy[i].id) {
                    nesting = i;
                    break;
                }
            }

            // Row is expanded by default
            group.className = "group group-expanded group-" + groupId;
            groupCell.setAttribute("colspan", numberOfColumns);
            if (Dom.hasClass(row, "yui-dt-first")) {
                // If this is the first row in the table, transfer the class to the group
                Dom.removeClass(row, "yui-dt-first");
                Dom.addClass(group, "group-first");
            }

            // Add a liner as per standard YUI cells
            var liner = document.createElement("div");
            liner.style.padding = '4px 10px 0px ' + (10+20*nesting) + 'px';
            liner.className = "liner";

            // Add icon
            icon.className = "icon";
            liner.appendChild(icon);

            // Add label
            var label = document.createElement("div");
            label.innerHTML = name ? name : this.MSG_NOGROUP;
            label.className = "label";
            liner.appendChild(label);
            groupCell.appendChild(liner);
            group.appendChild(groupCell);

            // Insert the group
            Dom.insertBefore(group, row);

            // Attach visibility toggle to liner click
            Event.addListener(liner, "click", this.toggleVisibility, this);

            // Set up DOM events
            if (name.length > 0) { // Only if the group has a value
                Event.addListener(group, "mouseover", this.onGroupMouseover, this);
                Event.addListener(group, "mouseout", this.onGroupMouseout, this);
                Event.addListener(group, "mousedown", this.onGroupMousedown, this);
                Event.addListener(group, "mouseup", this.onGroupMouseup, this);
                Event.addListener(group, "click", this.onGroupClick, this);
                Event.addListener(group, "dblclick", this.onGroupDblclick, this);
            }
            else {
                // Disable the group
                Dom.addClass(group, "group-disabled");
            }

            return group;
        },

        /**
         * Handles the group select event.
         * @method onEventSelectGroup
         * @param type {String} The type of event fired.
         * @param e {Object} The selected group.
         * @private
         */
        onEventSelectGroup: function(args) {
            this.selectGroup(args);
        },

        /**
         * Selects a group.
         * @method selectGroup
         */
        selectGroup: function(args) {
            var target = args.target;
            var groupRow = this.getTrEl(target);

            // Do not re-select if already selected
            if (!this.selectedGroup || groupRow !== this.selectedGroup) {
                // Unselect any previous group
                this.unselectGroup(args);

                // Select the new group
                Dom.addClass(groupRow, "group-selected");
                this.selectedGroup = groupRow;

                // Unselect all rows in the data table
                var selectedRows = this.getSelectedTrEls();

                for (var i = 0; i < selectedRows.length; i++) {
                    this.unselectRow(selectedRows[i]);
                }

                var record = this.getGroupRecord(groupRow);
                this.fireEvent("groupSelectEvent", { record: record, el: groupRow });
            }
        },

        /**
         * Unselects any selected group.
         * @method unselectGroup
         */
        unselectGroup: function(args) {
            var target = args.target;
            var row = this.getTrEl(target);

            if (this.selectedGroup && row !== this.selectedGroup) {
                Dom.removeClass(this.selectedGroup, "group-selected");

                var record = this.getGroupRecord(this.selectedGroup);
                this.fireEvent("groupUnselectEvent", { record: record, el: this.selectedGroup });

                this.selectedGroup = null;
            }
        },

        /**
         * Toggles the visibility of the group specified in the event.
         * @method toggleVisibility
         * @param e {Event} The event fired from clicking the group.
         * @private
         */
        toggleVisibility: function(e, self) {
            var group = Dom.getAncestorByClassName(Event.getTarget(e), "group");
            var groupId = "";
            var nesting = 0;
            for(var i=0; i<self.groupsBy.length; i++) {
                if(Dom.hasClass(group, "group-"+ self.groupsBy[i].id)) {
                    groupId = "group-"+ self.groupsBy[i].id;
                    nesting = i;
                    break;
                }
            }
            var visibleState;

            // Change the class of the group
            if (Dom.hasClass(group, "group-expanded")) {
                visibleState = false;
                Dom.replaceClass(group, "group-expanded", "group-collapsed");
                self.fireEvent("groupCollapseEvent", { target: group, event: e });
            }
            else {
                visibleState = true;
                Dom.replaceClass(group, "group-collapsed", "group-expanded");
                self.fireEvent("groupExpandEvent", { target: group, event: e });
            }

            // Hide all subsequent rows in the group
            var row = Dom.getNextSibling(group);
            while (row && !Dom.hasClass(row, groupId)) {
                var isNextGroup = false;
                for(var j=0; j<self.groupsBy.length; j++) {
                    if(Dom.hasClass(row, "group-" + self.groupsBy[j].id) && j <= nesting) {
                        isNextGroup = true;
                    }
                }
                if(!isNextGroup) {
                    if (visibleState) {
                        if(Dom.hasClass(row, "group-collapsed")) {
                            Dom.replaceClass(row, "group-collapsed", "group-expanded");
                        }
                        row.style.display = "table-row";
                    }
                    else {
                        row.style.display = "none";
                    }
                } else {
                    break;
                }

                row = Dom.getNextSibling(row);
            }
        },

        /**
         * For the given group identifier, returns the associated Record instance.
         * @method getGroupRecord
         * @param row {Object} DOM reference to a group TR element.
         * @private
         */
        getGroupRecord: function(groupRow) {
            for (var i = 0; i < this.groups.length; i++) {
                if (this.groups[i].group === groupRow) {
                    return this.groups[i].record;
                }
            }
        },

        getPreviousTrEl: function(row) {
            var currentRow = row;
            var previousRow = GroupedDataTable.superclass.getPreviousTrEl.call(this, currentRow);
            var firstRow = this.getFirstTrEl();

            while (previousRow !== firstRow) {
                if (Dom.hasClass(previousRow, "group")) {
                    previousRow = GroupedDataTable.superclass.getPreviousTrEl.call(this, previousRow);
                } else {
                    return previousRow;
                }
            }

            return currentRow;
        },

        getNextTrEl: function(row) {
            var nextRow = GroupedDataTable.superclass.getNextTrEl.call(this, row);
            var lastRow = this.getLastTrEl();

            while (nextRow !== lastRow) {
                if (Dom.hasClass(nextRow, "group")) {
                    nextRow = GroupedDataTable.superclass.getNextTrEl.call(this, nextRow);
                } else {
                    return nextRow;
                }
            }

            return lastRow;
        },

        onGroupMouseover: function(e, self) {
            self.fireEvent("groupMouseoverEvent", { target: Event.getTarget(e), event: e });
        },

        onGroupMouseout: function(e, self) {
            self.fireEvent("groupMouseoutEvent", { target: Event.getTarget(e), event: e });
        },

        onGroupMousedown: function(e, self) {
            self.fireEvent("groupMousedownEvent", { target: Event.getTarget(e), event: e });
        },

        onGroupMouseup: function(e, self) {
            self.fireEvent("groupMouseupEvent", { target: Event.getTarget(e), event: e });
        },

        onGroupClick: function(e, self) {
            self.fireEvent("groupClickEvent", { target: Event.getTarget(e), event: e });
        },

        onGroupDblclick: function(e, self) {
            self.fireEvent("groupDblclickEvent", { target: Event.getTarget(e), event: e });
        },

        onGroupSelect: function(e, self) {
            self.fireEvent("groupSelectEvent", { target: Event.getTarget(e), event: e });
        }

    });
})();