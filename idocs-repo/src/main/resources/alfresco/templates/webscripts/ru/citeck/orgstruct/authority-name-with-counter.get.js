var department = args.department,
    typeValue = args.typeValue,
    maxCounterValue = 0,
    authorityContainersCount = 0,
    groupNameWithoutCounter = 'GROUP_' + department + '_' + typeValue;

var authorityContainers = search.query({
    query: 'TYPE:"cm:authorityContainer" AND @cm\\:authorityName:"' + groupNameWithoutCounter + '*"',
    language: 'fts-alfresco',
    page: {
        maxItems: 10000
    }
});

if (authorityContainers && authorityContainers.length) {
    authorityContainersCount = authorityContainers.length;
    authorityContainers.forEach(function(authorityContainer) {
        var authFullName = authorityContainer.properties['cm:authorityName'];
        if (authFullName) {
            var authFullNameParts = authFullName.split('_');
            if (authFullNameParts.length > 0) {
                var counterStr = authFullNameParts[authFullNameParts.length-1],
                    counterVal = parseInt(counterStr) || 0;
                if (counterVal > maxCounterValue) {
                    maxCounterValue = counterVal;
                }
            }
        }
    });
    if (authorityContainersCount > 0) {
        maxCounterValue++;
    }
}

model.result = {
    value: groupNameWithoutCounter + '_' + maxCounterValue
};


