import { generateSearchTerm } from './util';

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

const postOptions = {
    ...getOptions,
    method: 'post'
};

export default class {
    constructor(alfrescoProxyUri) {
        this.alfrescoProxyUri = alfrescoProxyUri;
    }

    getJSON = url => {
        return fetch(this.alfrescoProxyUri + url, getOptions)
            .then(handleErrors)
            .then(toJson);
    };

    getPhotoSize = userNodeRef => {
        const url = "citeck/node?nodeRef=" + userNodeRef + "&props=ecos:photo";
        return this.getJSON(url)
            .then(data => data.props["ecos:photo"].size)
            .catch(() => 0);
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
        return this.getJSON(url).catch(() => {});
    };

    getLiveSearchSites = terms => {
        const url = "slingshot/live-search-sites?t=" + generateSearchTerm(terms) + "&maxResults=5";
        return this.getJSON(url).catch(() => {});
    };

    getLiveSearchPeople = terms => {
        const url = "slingshot/live-search-people?t=" + generateSearchTerm(terms) + "&maxResults=5";
        return this.getJSON(url).catch(() => {});
    };

    getSlideMenuItems = () => {
        const url = "citeck/menu/menu";
        return this.getJSON(url).catch(() => []);
    };

    getMenuItemIconUrl = (iconName) => {
        const url = `citeck/menu/icon?iconName=${iconName}`;
        return this.getJSON(url).catch(() => null);
    };
}