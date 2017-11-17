package ru.citeck.ecos.search;

public class FtsAlfrescoQueryMigration extends LuceneQuery {

    @Override
    public String buildQuery(SearchCriteria criteria) {
        QueryBuilder queryBuilder = new QueryBuilder() {
            @Override
            protected void buildRangeTerm(String field, String value, boolean inclusive, boolean lessThan) {
                StringBuilder range = new StringBuilder();
                range.append(inclusive ? "[" : "<");
                if (lessThan) {
                    range.append(FROM_MIN);
                }
                range.append(value);
                if (!lessThan) {
                    range.append(TO_MAX);
                }
                range.append(inclusive ? "]" : ">");

                StringBuilder term = new StringBuilder();
                term.append(field).append(SEPARATOR).append(range);
                queryElement.setQueryPart(term.toString());
                if (shouldAppendQuery) {
                    query.append(term);
                }
            }
        };
        
        return queryBuilder.buildQuery(criteria);
    }
}