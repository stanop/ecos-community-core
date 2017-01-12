const PREF_LAST_SITE = "org.alfresco.share.sites.last";

/**
 * Preferences
 */
function getPrefs()
{
    var collapsedTwisters = "",
        lastsite = "",
        result,
        response;

    result = remote.call("/api/people/" + encodeURIComponent(user.name) + "/preferences");
    if (result.status == 200 && result != "{}")
    {
        response = eval('(' + result + ')');
    }
    return response;
}

/**
 * User sites
 */
function getUserSites()
{
   var sites = [],
      result,
      response;

   result = remote.call("/api/people/" + encodeURIComponent(user.name) + "/sites");
   if (result.status == 200 && result != "{}")
   {
      response = eval('(' + result + ')');
      sites = eval('try{(response)}catch(e){}');
      if (typeof sites != "object" || !sites.length)
      {
         sites = [];
      }
   }
   return sites;
}

function site_exists(site) {
    return site && remote.call('/api/sites/' + site).status == 200;
}

function is_site_member(site) {
    return site && remote.call('/api/sites/' + site + '/memberships/' + encodeURIComponent(user.name)).status == 200;
}

/**
 * Get the last visited site.
 */
function getLastSite()
{
    // get current site:
    var site = page.url.templateArgs.site || "", 
        lastsite = "";
    
    // if we are on some existent site:
    if(site_exists(site)) {
    
        // show links of this site:
        lastsite = site;
    
    // if we are not on site:
    } else {
    
        // take it from preferences:
        var prefs = getPrefs();
        lastsite = eval('try{(prefs.' + PREF_LAST_SITE + ')}catch(e){}');
        if (typeof lastsite != "string") {
            lastsite = "";
        }
    
    }
    
    // if we are not member of last site
    if(!is_site_member(lastsite)) {
        // forget it
        lastsite = "";
    }
    
    // if last site is not set yet
    if(lastsite == "") {
        var sites = getUserSites();
        if(sites.length > 0) {
            // set the first site of user 
            lastsite = sites[0].shortName;
        } else {
            // or nothing
            lastsite = "";
        }
    }

    // update preferences
    if(lastsite) {
        remote.connect().post(
            "/api/people/" + encodeURIComponent(user.name) + "/preferences",
            '{ org: { alfresco: { share: { sites: { last: "'+lastsite+'" } } } } }', 
            "application/json"
        );
    }
    
    // finally return lastsite
    return lastsite;
}
