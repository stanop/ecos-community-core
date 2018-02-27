<#macro renderOrgstructBody el>
<div id="${el}-body" class="orgstruct-console">

    <div id="${el}-tree-resizer" class="orgstruct-tree-resizer" style="width: 20%; min-width: 380px;">
        <div class="orgstruct-config-selector">
            <button type="button" id="${el}-filter"></button>
            <select id="${el}-filter-select" name="filter-select">
            </select>
        </div>
        <div id="${el}-tree-toolbar" class="toolbar theme-bg-2">
            <div>
			<span id="${el}-tree-toolbar-button-styles">
				<input id="${el}-search-input" type="text" class="search-input"/>
				<span id="${el}-tree-toolbar-buttons" class="toolbar-buttons"></span>
			</span>
            </div>
        </div>
        <div id="${el}-tree" class="dynamic-tree-list dynamic-tree color-hover color-highlight hide-buttons">
        </div>
    </div>

    <div id="${el}-selected-item-details" class="selected-item-details">
        <div id="${el}-list-toolbar" class="toolbar theme-bg-2">
            <div>
                <span id="${el}-list-toolbar-buttons" class="toolbar-buttons"></span>
            </div>
        </div>
        <div id="${el}-list" class="dynamic-tree-list dynamic-list color-hover color-highlight hide-buttons">
        </div>
    </div>

    <!-- People Finder Dialog -->
    <div id="${el}-peoplepicker" class="people-picker" style="visibility: hidden;">
        <div class="hd"><span id="${el}-peoplepicker-title">${msg("panel.adduser.header")}</span></div>
        <div class="bd">
            <div style="margin: auto 10px;">
                <div id="${el}-peoplepicker-finder"></div>
            </div>
        </div>
    </div>

    <!-- Group Finder Dialog -->
    <div id="${el}-grouppicker" class="group-picker" style="visibility: hidden;">
        <div class="hd"><span id="${el}-grouppicker-title">${msg("panel.addgroup.header")}</span></div>
        <div class="bd">
            <div style="margin: auto 10px;">
                <div id="${el}-grouppicker-finder"></div>
            </div>
        </div>
    </div>

    <!-- Delete Item Dialog -->
    <div id="${el}-delete-dialog" class="delete-dialog" style="visibility: hidden;">
        <div class="hd">${msg("panel.deleteitem.header")}</div>
        <div class="bd">
            <div class="dialog-panel">
                <div>
                    <span id="${el}-delete-dialog-message"></span>
                </div>
            </div>
            <div class="bdft">
                <input type="submit" id="${el}-delete-dialog-delete-button" value="${msg("button.delete")}"/>
                <input type="button" id="${el}-delete-dialog-cancel-button" value="${msg("button.cancel")}"/>
            </div>
        </div>
    </div>

</div>
</#macro>