import { generateSearchTerm, getCurrentLocale } from './util';
import MenuApi from 'ecosui!menu-api';

function handleErrors(response) {
    if (!response.ok) {
        throw Error(`${response.status} (${response.statusText})`);
    }
    return response;
}

function toJson(response) {
    return response.json();
}

const getOptions = {
    credentials: 'include',
    method: 'get'
};

const menuApi = new MenuApi();

export default class {
    constructor(alfrescoProxyUri) {
        this.alfrescoProxyUri = alfrescoProxyUri;
    }

    getJSON = url => {
        return fetch(this.alfrescoProxyUri + url, {
            ...getOptions,
            headers: {
                'Accept-Language': getCurrentLocale()
            }
        })
            .then(handleErrors)
            .then(toJson);
    };

    getPhotoSize = userNodeRef => {
        const url = "citeck/node?nodeRef=" + userNodeRef + "&props=ecos:photo";
        return this.getJSON(url)
            .then(data => data.props["ecos:photo"].size)
            .catch(() => 0);
    };

    getNewJournalsPageEnable = () => {
        var isCurrentUserInGroup = function isCurrentUserInGroup(group) {
            var currentPersonName = Alfresco.constants.USERNAME;
            return Citeck.Records.queryOne({
                "query": 'TYPE:"cm:authority" AND =cm:authorityName:"' + group + '"',
                "language": "fts-alfresco"
            }, 'cm:member[].cm:userName').then(function (usernames) {
                return (usernames || []).indexOf(currentPersonName) != -1
            });
        };
        var checkJournalsAvailability = function isShouldDisplayJournals() {
            return Citeck.Records.get("ecos-config@default-ui-new-journals-access-groups")
                .load(".str").then(function(groupsInOneString) {

                    if (!groupsInOneString) {
                        return false;
                    }

                    var groups = groupsInOneString.split(',');
                    var results = [];
                    for(var groupsCounter = 0; groupsCounter < groups.length; ++groupsCounter) {
                        results.push(isCurrentUserInGroup.call(this, groups[groupsCounter]));
                    }
                    return Promise.all(results).then(function (values) {
                        return values.indexOf(false) == -1;
                    });
                });
        };
        var checkJournalsAvailabilityForUser = function isShouldDisplayJournalForUser() {
            return Citeck.Records.get("ecos-config@default-ui-main-menu").load(".str").then(function(result) {
                if (result == "left") {
                    return checkJournalsAvailability.call(this);
                }
                return false;
            });
        };


        const isNewJournalPageEnable = Citeck.Records.get('ecos-config@new-journals-page-enable').load('.bool');
        const isJournalAvailibleForUser = checkJournalsAvailabilityForUser.call(this);

        return  Promise.all([isNewJournalPageEnable, isJournalAvailibleForUser]).then(function (values) {
            return values[0] || values[1];
        });
    };

    getSitesForUser = username => {
        const url = "api/people/" + encodeURIComponent(username) + "/sites";
        return this.getJSON(url).catch(() => []);
    };

    getCreateVariantsForSite = sitename => {
        const url = "api/journals/create-variants/site/" + encodeURIComponent(sitename);
        return this.getJSON(url).then(resp => resp.createVariants).catch(() => []);
    };

    getCreateVariantsForAllSites = () => {
        const url = "api/journals/create-variants/site/ALL";
        return this.getJSON(url).catch(() => []);
    };

    getSiteData = (siteId) => {
        const url = "api/sites/" + siteId;
        return this.getJSON(url).catch(() => {});
    };

    getSiteUserMembership = (siteId, username) => {
        const url = "api/sites/" + siteId + "/memberships/" + encodeURIComponent(username);
        return this.getJSON(url).catch(() => {});
    };

    getLiveSearchDocuments = (terms, startIndex) => {
        const url = "slingshot/live-search-docs?t=" + generateSearchTerm(terms) + "&maxResults=5&startIndex=" + startIndex;
        return this.getJSON(url).catch(() => ({items: [], hasMoreRecords: false}));
    };

    getLiveSearchSites = terms => {
        const url = "slingshot/live-search-sites?t=" + generateSearchTerm(terms) + "&maxResults=5";
        return this.getJSON(url).catch(() => ({items: []}));
    };

    getLiveSearchPeople = terms => {
        const url = "slingshot/live-search-people?t=" + generateSearchTerm(terms) + "&maxResults=5";
        return this.getJSON(url).catch(() => ({items: []}));
    };

    getSlideMenuItems = () => {
        return menuApi.getSlideMenuItems();
    };

    getMenuItemIconUrl = (iconName) => {
        return menuApi.getMenuItemIconUrl(iconName);
    };
}