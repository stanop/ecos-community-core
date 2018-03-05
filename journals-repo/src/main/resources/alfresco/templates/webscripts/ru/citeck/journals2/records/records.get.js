
function main() {

    var language = "fts-alfresco";
    var journalType = journals.getJournalType(args['journalId']);

    if (journalType.options['datasourceType'] == 'graphql') {

        var graphqlService = services.get("graphQLService");

        var result = graphqlService.execute(
            '{criteriaSearch(q:"' + args['query'].replace(/"/g,'\\"') + '", lang:"' + language + '") {' +
                'paging{hasMore, maxItems, skipCount, totalCount, totalItems} ' +
                'query {language, value}' +
                'results {...journalFields}' +
            '}} ' + journalType.getFieldsSchema()
        ).toSpecification();

        var results = result.data.criteriaSearch.results;
        var processedResults = [];
        var resIt = results.iterator();
        while (resIt.hasNext()) {
            var record = resIt.next();
            var processedResult = {
                attributes: {}
            };
            for (var key in record) {
                if (key.indexOf("_") > 0) {
                    var value = record[key];
                    processedResult.attributes[key.replace(/_/g, ':')] = value.nodes || value.value;
                } else {
                    processedResult[key] = record[key];
                }
            }
            processedResults.push(processedResult);
        }

        model.result = {
            paging: result.data.criteriaSearch.paging,
            query: result.data.criteriaSearch.query,
            results: processedResults
        }

    } else {

        var data = criteriaSearch.query(args['query'], language);

        for (var key in data) {
            model[key] = data[key];
        }

        model.language = language;

        var globalProps = services.get("global-properties");
        model.personFirstName = globalProps["reportProducer.personFirstName"];
        model.personLastName = globalProps["reportProducer.personLastName"];
        model.personMiddleName = globalProps["reportProducer.personMiddleName"];
    }

    model.datasourceType = journalType.options['datasourceType'];
}

main();