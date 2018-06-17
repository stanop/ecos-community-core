package ru.citeck.ecos.webscripts.people;


import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import ru.citeck.ecos.model.EcosModel;
import ru.citeck.ecos.search.SearchQuery;
import ru.citeck.ecos.utils.RepoUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpcomingBirthdaysGet extends DeclarativeWebScript {
    private NodeService nodeService;
    private SearchService searchService;

    private static final String KEY_UPCOMING_BIRTHDAYS = "upcomingBirthdays";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FIRSTNAME = "firstname";
    private static final String KEY_LASTNAME = "lastname";
    private static final String KEY_BIRTHDATE = "birthdate";
    private static final String KEY_HASPHOTO = "hasphoto";
    private static final String KEY_ID = "id";

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Integer currentMonthDay = getCurrentMonthDay();

        String query = "TYPE:\"cm:person\" AND @ecos:birthMonthDay:[" + currentMonthDay + " TO " + (currentMonthDay + 100) + "]";

        List<NodeRef> result = search(query);
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_UPCOMING_BIRTHDAYS, createJsonResponse(result));
        return map;
    }

    private Integer getCurrentMonthDay() {
        LocalDate localDate = LocalDate.now();
        return localDate.getMonthValue() * 100 + localDate.getDayOfMonth();
    }

    private List<NodeRef> search(String query) {
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setQuery(query);
        searchParameters.setLanguage(SearchQuery.DEFAULT_LANGUAGE);
        searchParameters.setMaxItems(100);
        searchParameters.setSkipCount(0);
        searchParameters.addSort(EcosModel.PROP_BIRTH_MONTH_DAY.toString(), true);

        ResultSet resultSet = searchService.query(searchParameters);
        return resultSet.getNodeRefs();
    }

    private String createJsonResponse(List<NodeRef> nodeRefs) {
        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();

        try {
            result.put("data", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (NodeRef nodeRef : nodeRefs) {
            array.put(getPersonInfo(nodeRef));
        }

        return result.toString();
    }

    private Map<String, String> getPersonInfo(NodeRef nodeRef){
        Map<String, String> personInfo = new HashMap<>(4);
        personInfo.put(KEY_USERNAME, RepoUtils.getProperty(nodeRef, ContentModel.PROP_USERNAME, nodeService));
        personInfo.put(KEY_FIRSTNAME, RepoUtils.getProperty(nodeRef, ContentModel.PROP_FIRSTNAME, nodeService));
        personInfo.put(KEY_LASTNAME, RepoUtils.getProperty(nodeRef, ContentModel.PROP_LASTNAME, nodeService));
        Date birthDate = RepoUtils.getProperty(nodeRef, EcosModel.PROP_BIRTH_DATE, nodeService);
        personInfo.put(KEY_BIRTHDATE, dateToString(birthDate));
        personInfo.put(KEY_HASPHOTO, hasPhoto(nodeRef));
        personInfo.put(KEY_ID, nodeRef.getId());
        return personInfo;
    }

    private String hasPhoto(NodeRef nodeRef) {
        if (RepoUtils.getProperty(nodeRef, EcosModel.PROP_PHOTO, nodeService) != null) {
            return "true";
        }
        return "false";
    }

    private String dateToString(Date date) {
        if (date == null) {
            return null;
        }
        DateFormat df = new SimpleDateFormat("YYYY-MM-dd");
        return df.format(date);
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }
}
