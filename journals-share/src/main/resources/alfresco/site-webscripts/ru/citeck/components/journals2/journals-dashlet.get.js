<import resource="classpath:alfresco/site-webscripts/ru/citeck/components/journals2/journals.lib.js">
fillModel();

var isPrivileged = true;
if (!user.isAdmin)
{
  if (page.url.templateArgs.site)
  {
	 isPrivileged = false;
	 // We are in the context of a site, so call the repository to see if the user is site manager or not
	 var json = remote.call("/api/sites/" + page.url.templateArgs.site + "/memberships/" + encodeURIComponent(user.name));

	 if (json.status == 200)
	 {
		var obj = eval('(' + json + ')');
		if (obj)
		{
		   isPrivileged = obj.role == "SiteManager";
		}
	 }
  }
}
model.isPrivileged = isPrivileged;
