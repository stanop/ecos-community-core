export const selectedMenuItemIdKey = 'selectedMenuItemId';

// TODO delete
export function processApiData(oldItems) {
    if (!oldItems) {
        return null;
    }

    return oldItems.map(item => {
        let newItem = { ...item };
        delete newItem['widgets'];
        if (item.widgets) {
            newItem.items = processApiData(item.widgets);
        }

        return newItem;
    });
}

export function processSlideMenuApiData(oldItems, parent) {
    if (!oldItems) {
        return [];
    }

    return oldItems.map(item => {
        let newItem = { ...item };

        let itemId = item.id || '';

        if (item.action) {
            switch (item.action.type) {
                case 'CREATE_SITE':
                    break;
                case 'FILTER_LINK':
                    if (parent && parent.action && parent.action.type === 'JOURNAL_LINK') {
                        itemId = `HEADER_${(`${parent.id}_${item.label}`.replace(/-/g, "_")).toUpperCase()}_FILTER`;
                    }
                    break;
                case 'JOURNAL_LINK':
                    if (parent && parent.action && parent.action.type === 'SITE_LINK') {
                        // HEADER_ORDERS_ORDERS_INTERNAL_JOURNAL
                        itemId = `HEADER_${(`${parent.action.params.siteName}_${item.id}`.replace(/-/g, "_")).toUpperCase()}_JOURNAL`;
                    } else {
                        // HEADER_TASKS_ACTIVE_TASKS_JOURNAL
                        itemId = `HEADER_TASKS_${(`${itemId}`.replace(/-/g, "_")).toUpperCase()}_JOURNAL`;
                    }
                    break;
                case 'PAGE_LINK':
                    if (item.action.params.context) {

                    }
                    break;
                case 'SITE_LINK':
                    itemId = `HEADER_${(`${itemId}`.replace(/-/g, "_")).toUpperCase()}`;
                    break;
            }
        }

        newItem.id = itemId.split(' ').join('_');

        newItem.items = processSlideMenuApiData(item.items, item);
        return newItem;
    });
}

export function fetchExpandableItems(items, selectedId) {
    let flatList = [];
    items.map(item => {
        const hasNestedList = !!item.items;
        if (hasNestedList) {
            let isNestedListExpanded = !!item.sectionTitle || hasChildWithId(item.items, selectedId); // TODO delete !!item.sectionTitle ||
            flatList.push(
                {
                    id: item.id,
                    hasNestedList,
                    isNestedListExpanded,
                },
                ...fetchExpandableItems(item.items, selectedId)
            );
        }
    });

    return flatList;
}

export function hasChildWithId(items, selectedId) {
    let childIndex = items.findIndex(item => item.id === selectedId);
    if (childIndex !== -1) {
        return true;
    }

    let totalItems = items.length;

    for (let i = 0; i < totalItems; i++) {
        if (!items[i].items) {
            continue;
        }

        let hasChild = hasChildWithId(items[i].items, selectedId);
        if (hasChild) {
            return true;
        }
    }

    return false;
}