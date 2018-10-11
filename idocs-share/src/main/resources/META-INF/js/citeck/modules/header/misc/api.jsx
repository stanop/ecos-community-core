import { generateSearchTerm } from '../misc/util';

// TODO include polyfills

// const catchError = err => console.error(err);

export default class {
    constructor(alfrescoProxyUri) {
        this.alfrescoProxyUri = alfrescoProxyUri;
    }

    getJSON(url) {
        return new Promise((resolve, reject) => {
            const http = new XMLHttpRequest();
            http.open('GET', this.alfrescoProxyUri + url, true);

            http.onload = function() {
                if (http.status === 200) {
                    try {
                        resolve(JSON.parse(http.responseText));
                    } catch(e) {
                        reject(Error('JSON is not valid'));
                    }
                } else {
                    reject(Error(http.statusText));
                }
            };

            http.onerror = function() {
                reject(Error('Network error'))
            };

            http.send();
        });
    }

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
}