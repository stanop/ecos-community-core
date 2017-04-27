<import resource="classpath:alfresco/templates/webscripts/ru/citeck/orgstruct/orgstruct.lib.js">
(function() {
    var maxItems = 100,
        authorities = [],
        skipCount = 0,
        filter = getFilterOptions(),
        result;
    if(args.root == "true") {
        do {
            result = groups.getAllRootGroups(maxItems, skipCount);
            authorities = authorities.concat(filterAuthorities(result, filter));
            skipCount += maxItems;
        } while(result.length > 0);
    } else {
        do {
            var paging = utils.createPaging(maxItems, skipCount);
            try {
                result = groups.getGroups(null, paging);
            } catch(e) {
                result = [];
            }
            authorities = authorities.concat(filterAuthorities(result, filter));
            skipCount += result.length;
        } while(result.length > 0);
    }
	model.authorities = authorities;
})();