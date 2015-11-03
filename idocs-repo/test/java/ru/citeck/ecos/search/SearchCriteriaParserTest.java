package ru.citeck.ecos.search;

import org.alfresco.model.ContentModel;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import ru.citeck.ecos.test.ApplicationContextHelper;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class SearchCriteriaParserTest {

    private SearchCriteriaParser parser;

    private String jsonString;

    private SearchCriteria expectedCriteria;

    @Test(expected = IllegalStateException.class)
    public void parseCriteriaWrongType() {
        parser.parse(new HashMap<String, String>());
        fail("Search criteria is built from unknown type");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseCriteriaWrongString() throws JSONException {
        String wrongJsonString = "wrong JSON string";
        parser.parse(wrongJsonString);
    }

    @Test
    public void parseCriteriaString() {
        SearchCriteria actualCriteria = parser.parse(jsonString);
        assertEquals("Criteria are not equal", expectedCriteria, actualCriteria);
    }

    @Test
    public void parseCriteriaJson() throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        SearchCriteria actualCriteria = parser.parse(jsonObject);
        assertEquals("Criteria are not equal", expectedCriteria, actualCriteria);
    }

    @Before
    public void setUp() {
        ApplicationContext context = ApplicationContextHelper.getApplicationContext();
        SearchCriteriaFactory criteriaFactory = context.getBean("searchCriteriaFactory", SearchCriteriaFactory.class);
        parser = context.getBean("criteriaParser", SearchCriteriaParser.class);
        jsonString = new StringBuilder()
                .append("{")
                .append("\"field_0\": \"TYPE\",")
                .append("\"field_1\": \"cm:name\",")
                .append("\"predicate_0\": \"type-equals\",")
                .append("\"value_0\": \"cm:content\",")
                .append("\"value_1\": \"blah\",")
                .append("\"skipCount\": \"50\",")
                .append("\"maxItems\": \"50\",")
                .append("\"predicate_1\": \"string-contains\",")
                .append("\"sortBy\": [{\"attribute\": \"cm:name\", \"order\": \"asc\"}, {\"attribute\": \"cm:creator\", \"order\": \"desc\"}]")
                .append("}").toString();
        expectedCriteria = criteriaFactory.createSearchCriteria()
                .addCriteriaTriplet(FieldType.TYPE, SearchPredicate.TYPE_EQUALS, ContentModel.PROP_CONTENT)
                .addCriteriaTriplet(ContentModel.PROP_NAME, SearchPredicate.STRING_CONTAINS, "blah")
                .addSort(ContentModel.PROP_NAME, SortOrder.ASCENDING)
                .addSort(ContentModel.PROP_CREATOR, SortOrder.DESCENDING)
                .setLimit(50)
                .setSkip(50);
    }

}
