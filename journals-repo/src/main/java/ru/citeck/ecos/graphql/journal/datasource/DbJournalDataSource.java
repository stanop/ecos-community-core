package ru.citeck.ecos.graphql.journal.datasource;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JGqlPageInfo;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.record.JGqlRecordsConnection;
import ru.citeck.ecos.graphql.meta.value.MetaExplicitValue;
import ru.citeck.ecos.graphql.meta.value.MetaValue;

import javax.sql.DataSource;
import java.sql.ResultSetMetaData;
import java.util.*;

public class DbJournalDataSource implements JournalDataSource {

    private NamedParameterJdbcTemplate template;

    private String sqlQueryTemplate;

    @Override
    public JGqlRecordsConnection getRecords(GqlContext context,
                                            String query,
                                            String language,
                                            JGqlPageInfoInput pageInfo) {

        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("skipCount", pageInfo.getSkipCount());
        namedParameters.addValue("maxItems", pageInfo.getMaxItems());

        String sqlQuery = sqlFromTemplate(sqlQueryTemplate, query, language);

        List<MetaValue> records = template.query(sqlQuery, namedParameters, (resultSet, i) -> {
            ResultSetMetaData metaData = resultSet.getMetaData();
            Map<String, String> attributes = new HashMap<>();
            for (int columnIdx = 1; columnIdx < metaData.getColumnCount(); columnIdx++) {
                attributes.put("cm:" + metaData.getColumnName(columnIdx), resultSet.getString(columnIdx));
            }
            return new RecordValue("" + hashCode() + pageInfo.getSkipCount() + i, attributes);
        });

        JGqlRecordsConnection connection = new JGqlRecordsConnection();

        JGqlPageInfo outPageInfo = new JGqlPageInfo();
        outPageInfo.setHasNextPage(true);
        outPageInfo.setMaxItems(pageInfo.getMaxItems());
        outPageInfo.setSkipCount(pageInfo.getSkipCount());

        connection.setRecords(records);
        connection.setPageInfo(outPageInfo);
        connection.setTotalCount(pageInfo.getMaxItems());

        return connection;
    }

    protected String sqlFromTemplate(String sqlQueryTemplate, String query, String language) {
        return sqlQueryTemplate;
    }

    public void setDataSource(DataSource dataSource) {
        template = new NamedParameterJdbcTemplate(dataSource);
    }

    public void setSqlQueryTemplate(String sqlQueryTemplate) {
        this.sqlQueryTemplate = sqlQueryTemplate;
    }

    private class RecordValue implements MetaValue {

        private String id;
        private Map<String, String> attributes;

        public RecordValue(String id, Map<String, String> attributes) {
            this.id = id;
            this.attributes = attributes;
        }

        @Override
        public String getId() {
            return String.valueOf(id);
        }

        @Override
        public String getString() {
            return getId();
        }

        @Override
        public List<MetaValue> getAttribute(String name) {
            return Collections.singletonList(new MetaExplicitValue(attributes.get(name)));
        }
    }
}
