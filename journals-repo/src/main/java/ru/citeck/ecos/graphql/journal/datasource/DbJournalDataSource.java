package ru.citeck.ecos.graphql.journal.datasource;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JGqlPageInfo;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeInfo;
import ru.citeck.ecos.graphql.journal.record.JGqlRecordsConnection;
import ru.citeck.ecos.graphql.journal.response.JournalData;
import ru.citeck.ecos.graphql.meta.attribute.MetaAttribute;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.journals.records.JournalRecordsResult;
import ru.citeck.ecos.records.RecordRef;

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

    @Override
    public String getServerId() {
        return null;
    }

    protected String sqlFromTemplate(String sqlQueryTemplate, String query, String language) {
        return sqlQueryTemplate;
    }

    @Override
    public JournalRecordsResult queryIds(GqlContext context,
                                         String query,
                                         String language,
                                         JGqlPageInfoInput pageInfo) {
        return null;
    }

    @Override
    public List<MetaValue> convertToGqlValue(GqlContext context,
                                                      List<RecordRef> remoteRefList) {
        return null;
    }

    @Override
    public JournalData queryMetadata(String gqlQuery,
                                     String dataSourceBeanName,
                                     JournalRecordsResult recordsResult) {
        return null;
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

    private class RecordValue implements MetaValue {

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
        public Optional<MetaAttribute> att(String name) {
            return Optional.of(new Att(name, attributes.get(name)));
        }

        @Override
        public List<MetaAttribute> atts(String filter) {
            return Collections.emptyList();
        }
    }

    private class Att implements MetaAttribute {

        private String name;
        private String value;

        public Att(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public List<MetaValue> val() {
            return Collections.singletonList(new AttrValue(value));
        }
    }

    private class AttrValue implements MetaValue {

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
        public Optional<MetaAttribute> att(String name) {
            return Optional.empty();
        }

        @Override
        public List<MetaAttribute> atts(String filter) {
            return Collections.emptyList();
        }
    }
}
