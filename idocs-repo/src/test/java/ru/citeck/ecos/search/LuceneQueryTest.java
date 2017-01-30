package ru.citeck.ecos.search;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
//import ru.citeck.ecos.test.ApplicationContextHelper;

import static org.junit.Assert.assertEquals;

/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class LuceneQueryTest {

    private static final String FIELD = "field";

    private static final String PREDICATE = "predicate";

    private static final String VALUE = "value";

    private LuceneQuery queryBuilder;

    private SearchCriteriaParser parser;

    private int fieldCounter;

    private int predicateCounter;

    private int valueCounter;

    @Test
    public void testEmptyQuery() {
//        String actualQuery = queryBuilder.buildQuery(parser.parse("{}"));
//        assertEquals("Non empty query", "", actualQuery);
    }

    @Test
    public void testEqualsQuery() throws JSONException {
//        JSONObject json = new JSONObject()
//                .put(getField(), "cm:name")
//                .put(getPredicate(), SearchPredicate.STRING_EQUALS.toString())
//                .put(getValue(), "Attorney form 1");
//        String actualQuery = queryBuilder.buildQuery(parser.parse(json));
//
//        String expectedQuery = "@cm\\:name:\"Attorney form 1\"";
//        assertEquals("Wrong equals query", expectedQuery, actualQuery);
    }

    @Test
    public void testNotEqualsQuery() throws JSONException {
//        JSONObject json = new JSONObject()
//                .put(getField(), "cm:name")
//                .put(getPredicate(), SearchPredicate.STRING_NOT_EQUALS.toString())
//                .put(getValue(), "Attorney form 1");
//        String actualQuery = queryBuilder.buildQuery(parser.parse(json));
//
//        String expectedQuery = "NOT @cm\\:name:\"Attorney form 1\"";
//        assertEquals("Wrong not equals query", expectedQuery, actualQuery);
    }

    @Test
    public void testNotNullQuery() throws JSONException {
//        JSONObject json = new JSONObject()
//                .put(getField(), "cm:name")
//                .put(getPredicate(), SearchPredicate.STRING_NOT_EMPTY)
//                .put(getValue(), "");
//        String actualQuery = queryBuilder.buildQuery(parser.parse(json));
//
//        String expectedQuery = "(ISNOTNULL:\"cm:name\" AND NOT @cm\\:name:\"\")";
//        assertEquals("Wrong not empty query", expectedQuery, actualQuery);
    }

    @Test
    public void testLessOrEqualQuery() throws JSONException {
//        JSONObject json = new JSONObject()
//                .put(getField(), "bpm:taskId")
//                .put(getPredicate(), SearchPredicate.NUMBER_LESS_OR_EQUAL)
//                .put(getValue(), "100");
//        String actualQuery = queryBuilder.buildQuery(parser.parse(json));
//
//        String expectedQuery = "@bpm\\:taskId:[MIN TO 100]";
//        assertEquals("Wrong less or equal query", expectedQuery, actualQuery);
    }

    @Test
    public void testAssocContainsQuery() throws JSONException {
//        JSONObject json = new JSONObject()
//                .put(getField(), "cm:basis")
//                .put(getPredicate(), SearchPredicate.ASSOC_CONTAINS)
//                .put(getValue(), "privilege1, privilege2");
//        String actualQuery = queryBuilder.buildQuery(parser.parse(json));
//
//        String expectedQuery = "(@cm\\:basis_added:\"privilege1\" OR @cm\\:basis_added:\"privilege2\")";
//        assertEquals("Wrong assoc contains query", expectedQuery, actualQuery);
    }

    @Test
    public void testComplexQuery() throws JSONException {
//        JSONObject json = new JSONObject()
//                .put(getField(), "path")
//                .put(getPredicate(), SearchPredicate.TYPE_EQUALS)
//                .put(getValue(), "/app:company_home/st:sites/cm:test/cm:documentLibrary/cm:journals/cm:attorney-forms/*")
//                .put(getField(), "cm:name")
//                .put(getPredicate(), SearchPredicate.STRING_STARTS_WITH)
//                .put(getValue(), "Attorney form")
//                .put(getField(), "sys:node-dbid")
//                .put(getPredicate(), SearchPredicate.NUMBER_GREATER_OR_EQUAL)
//                .put(getValue(), "21200")
//                .put(getField(), "att:isContractorForm")
//                .put(getPredicate(), SearchPredicate.ANY)
//                .put(getValue(), "");
//        String actualQuery = queryBuilder.buildQuery(parser.parse(json));
//
//        String expectedQuery = "PATH:\"/app:company_home/st:sites/cm:test/cm:documentLibrary/cm:journals/cm:attorney-forms/*\" AND @cm\\:name:\"Attorney form*\" " +
//                "AND @sys\\:node-dbid:[21200 TO MAX] AND @att\\:isContractorForm:\"*\"";
//        assertEquals("Wrong complex query", expectedQuery, actualQuery);
    }

    @Before
    public void setUp() {
//        ApplicationContext context = ApplicationContextHelper.getApplicationContext();
//        queryBuilder = context.getBean("luceneQueryBuilder", LuceneQuery.class);
//        parser = context.getBean("criteriaParser", SearchCriteriaParser.class);
    }

    @After
    public void tearDown() {
//        fieldCounter = 0;
//        predicateCounter = 0;
//        valueCounter = 0;
    }

    private String getField() {
        return FIELD + "_" + fieldCounter++;
    }

    private String getPredicate() {
        return PREDICATE + "_" + predicateCounter++;
    }

    private String getValue() {
        return VALUE + "_" + valueCounter++;
    }
}
