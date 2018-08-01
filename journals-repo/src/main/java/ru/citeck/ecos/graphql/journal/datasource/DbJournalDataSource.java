package ru.citeck.ecos.graphql.journal.datasource;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JGqlPageInfo;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.record.JGqlAttribute;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeInfo;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeValue;
import ru.citeck.ecos.graphql.journal.record.JGqlRecordsConnection;

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

        List<JGqlAttributeValue> records = template.query(sqlQuery, namedParameters, (resultSet, i) -> {
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

    @Override
    public Optional<JGqlAttributeInfo> getAttributeInfo(String attributeName) {
        return Optional.empty();
    }

    public void setDataSource(DataSource dataSource) {
        template = new NamedParameterJdbcTemplate(dataSource);
    }

    public void setSqlQueryTemplate(String sqlQueryTemplate) {
        this.sqlQueryTemplate = sqlQueryTemplate;
    }

    private class RecordValue implements JGqlAttributeValue {

        private String id;
        private Map<String, String> attributes;

        public RecordValue(String id, Map<String, String> attributes) {
            this.id = id;
            this.attributes = attributes;
        }

        @Override
        public String id() {
            return String.valueOf(id);
        }

        @Override
        public String str() {
            return id();
        }

        @Override
        public Optional<JGqlAttribute> attr(String name) {
            return Optional.of(new Attribute(name, attributes.get(name)));
        }
    }

    private class Attribute implements JGqlAttribute {

        private String name;
        private String value;

        public Attribute(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public List<JGqlAttributeValue> val() {
            return Collections.singletonList(new AttrValue(value));
        }
    }

    private class AttrValue implements JGqlAttributeValue {

        private String val;

        public AttrValue(String value) {
            val = value;
        }

        @Override
        public String id() {
            return null;
        }

        @Override
        public String str() {
            return val;
        }

        @Override
        public Optional<JGqlAttribute> attr(String name) {
            return Optional.empty();
        }
    }
}
