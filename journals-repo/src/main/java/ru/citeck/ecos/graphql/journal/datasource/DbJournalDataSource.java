package ru.citeck.ecos.graphql.journal.datasource;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JournalGqlPageInfo;
import ru.citeck.ecos.graphql.journal.JournalGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeGql;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeInfoGql;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeValueGql;
import ru.citeck.ecos.graphql.journal.record.JournalRecordsConnection;

import javax.sql.DataSource;
import java.sql.ResultSetMetaData;
import java.util.*;

public class DbJournalDataSource implements JournalDataSource {

    private NamedParameterJdbcTemplate template;

    private String sqlQuery;

    @Override
    public JournalRecordsConnection getRecords(GqlContext context,
                                               String query,
                                               String language,
                                               JournalGqlPageInfoInput pageInfo) {

        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("skipCount", pageInfo.getSkipCount());
        namedParameters.addValue("maxItems", pageInfo.getMaxItems());

        List<JournalAttributeValueGql> records = template.query(sqlQuery, namedParameters, (resultSet, i) -> {
            ResultSetMetaData metaData = resultSet.getMetaData();
            Map<String, String> attributes = new HashMap<>();
            for (int columnIdx = 1; columnIdx < metaData.getColumnCount(); columnIdx++) {
                attributes.put("cm:" + metaData.getColumnName(columnIdx), resultSet.getString(columnIdx));
            }
            return new RecordValue("" + hashCode() + pageInfo.getSkipCount() + i, attributes);
        });

        JournalRecordsConnection connection = new JournalRecordsConnection();

        JournalGqlPageInfo outPageInfo = new JournalGqlPageInfo();
        outPageInfo.setHasNextPage(true);
        outPageInfo.setMaxItems(pageInfo.getMaxItems());
        outPageInfo.setSkipCount(pageInfo.getSkipCount());

        connection.setRecords(records);
        connection.setPageInfo(outPageInfo);
        connection.setTotalCount(pageInfo.getMaxItems());

        return connection;
    }

    @Override
    public Optional<JournalAttributeInfoGql> getAttributeInfo(String attributeName) {
        return Optional.empty();
    }

    public void setDataSource(DataSource dataSource) {
        template = new NamedParameterJdbcTemplate(dataSource);
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    private class RecordValue implements JournalAttributeValueGql {

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
        public String data() {
            return id();
        }

        @Override
        public Optional<JournalAttributeGql> attr(String name) {
            return Optional.of(new Attribute(name, attributes.get(name)));
        }
    }

    private class Attribute implements JournalAttributeGql {

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
        public List<JournalAttributeValueGql> val() {
            return Collections.singletonList(new AttrValue(value));
        }
    }

    private class AttrValue implements JournalAttributeValueGql {

        private String val;

        public AttrValue(String value) {
            val = value;
        }

        @Override
        public String id() {
            return null;
        }

        @Override
        public String data() {
            return val;
        }

        @Override
        public Optional<JournalAttributeGql> attr(String name) {
            return Optional.empty();
        }
    }
}
