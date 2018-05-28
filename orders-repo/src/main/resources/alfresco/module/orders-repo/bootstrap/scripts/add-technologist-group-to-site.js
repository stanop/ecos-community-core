var groupOrdersTechnologist = people.getGroup("GROUP_orders-technologist");
var groupSiteOrdersManager = people.getGroup("GROUP_site_orders_SiteManager");

if (groupOrdersTechnologist && groupSiteOrdersManager) {
    people.addAuthority(groupSiteOrdersManager, groupOrdersTechnologist);
}