package ru.citeck.ecos.graphql.journal.datasource;

import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JournalGqlPageInfo;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeGql;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeInfoGql;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeValueGql;
import ru.citeck.ecos.graphql.journal.record.JournalRecordsConnection;

import java.util.*;

public class TestJournalRecordsSource implements JournalDataSource {

    @Override
    public JournalRecordsConnection getRecords(GqlContext context, String query, String language, String after, Integer first) {

        JournalRecordsConnection connection = new JournalRecordsConnection();

        List<JournalAttributeValueGql> records = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Map<String, String> attributes = new HashMap<>();

            attributes.put("cm:name", "Name Test " + i);
            attributes.put("cm:title", "Title Test 2 some data " + i);

            RecordValue value = new RecordValue(i, attributes);

            records.add(value);
        }

        connection.setRecords(records);

        JournalGqlPageInfo pageInfo = new JournalGqlPageInfo();
        pageInfo.setEndCursor("0");
        pageInfo.setHasNextPage(false);
        connection.setPageInfo(pageInfo);

        connection.setTotalCount(10);

        return connection;
    }

    @Override
    public Optional<JournalAttributeInfoGql> getAttributeInfo(String attributeName) {
        return Optional.empty();
    }

    private class RecordValue implements JournalAttributeValueGql {

        private int id;
        private Map<String, String> attributes;

        public RecordValue(int id, Map<String, String> attributes) {
            this.id = id;
            this.attributes = attributes;
        }

        @Override
        public String id() {
            return String.valueOf(id);
        }

        @Override
        public String disp() {
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
        public String disp() {
            return val;
        }

        @Override
        public Optional<JournalAttributeGql> attr(String name) {
            return Optional.empty();
        }
    }


}