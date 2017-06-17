function siteDetails(site) {

	var details = {
		"id": site,
		"title": "",
		"allowed": true
	};

	if(!site) return details;

	// try to find out, if site is accessible to user:
	
	// first get details of site:
	var siteinfo = remote.call('/api/sites/' + site);
	
	// no such site or other error:
	if(siteinfo.status != 200) {
		details.allowed = false;
		return details;
	}
	
	siteinfo = eval('('+siteinfo+')');
	details.title = siteinfo.title;
	
	// if site is public - it is allowed
	if(siteinfo.visibility == "PUBLIC" || siteinfo.visibility == "MODERATED") {
		return details;
	}
	
	// if site is not public - check membership
	var roleinfo = remote.call('/api/sites/' + site + '/memberships/' + encodeURIComponent(user.name));
	
	// no such member or other error:
	if(roleinfo.status != 200) {
		details.allowed = false;
	}
	
	// if there is some role - site is allowed
	return details;
}

model.site = siteDetails(page.url.templateArgs.site || "");
