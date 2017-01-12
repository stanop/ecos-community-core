/**
 * AllSites module GET method
 */

function main()
{
   var sites;

   // Call the repo for the user's sites
   var result = remote.call("/api/people/" + encodeURIComponent(user.name) + "/sites");
   if (result.status == 200 && result != "{}")
   {
      sites = eval('(' + result + ')');
   }

   if (typeof sites != "object")
   {
      sites = {};
   }

   // Prepare the model for the template
   model.userSites = sites;
}

main();