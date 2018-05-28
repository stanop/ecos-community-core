var data = [
    {path:"/app:company_home/app:dictionary/cm:cardlets/cm:cmobject-card-templates-right-e5"},
    {path:"/app:company_home/app:dictionary/cm:cardlets/activ:hasActivities-document-workflows-right-t3"},
    {path:"/app:company_home/app:dictionary/cm:cardlets/activ:hasActivities-inactive-document-workflows-right-t7"},
    {path:"/app:company_home/app:dictionary/cm:cardlets/activ:hasActivities-inactive-document-workflows-right-t7"},
    {path:"/app:company_home/app:dictionary/cm:cardlets/cm:content-document-workflows-right-t3"},
    {path:"/app:company_home/app:dictionary/cm:cardlets/cm:content-inactive-document-workflows-right-t7"},
    {path:"/app:company_home/app:dictionary/cm:cardlets/cm:content-inactive-document-workflows-right-t7"},
    {path:"/app:company_home/app:dictionary/cm:cardlets/req:hasCompletenessLevels-case-completeness-right-l5"},
    {path:"/app:company_home/app:dictionary/cm:cardlets/req:hasCompletenessLevels-case-levels-left-l5"},
    {path:"/app:company_home/app:dictionary/cm:cardlets/activ:hasActivities-case-tasks-left-h7"},
    {path:"/app:company_home/app:dictionary/cm:cardlets/icaseRole:role-case-roles-left-h8"},
    {path:"/app:company_home/app:dictionary/cm:cardmodes/cardlet:case-mgmt"},
    {path:"/app:company_home/app:dictionary/cm:cardlets/dms:hasSupplementaryFiles-supplementary-files-right-ba5"}
];

function hiddenCardlets() {
    for (var i in data) {
        var cardlet = search.selectNodes(data[i].path)[0];
        var auths = [];
        auths.push("GROUP_ALFRESCO_ADMINISTRATORS");
        cardlet.properties['cardlet:allowedAuthorities'] = auths;
        cardlet.save();
    }

}

hiddenCardlets();