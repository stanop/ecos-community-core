package ru.citeck.ecos.webscripts.people;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.model.EcosModel;
import ru.citeck.ecos.records.birthday.BirthdaysUtils;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Get people with upcoming birthdays in next 30 days.
 * Searching by {@code ecos:birthMonthDay} attribute
 */
public class UpcomingBirthdaysGet extends AbstractWebScript {

    private NodeService nodeService;
    private BirthdaysUtils birthdaysUtils;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String KEY_USERNAME = "username";
    private static final String KEY_FIRSTNAME = "firstname";
    private static final String KEY_LASTNAME = "lastname";
    private static final String KEY_BIRTHDATE = "birthdate";
    private static final String KEY_HASPHOTO = "hasphoto";
    private static final String KEY_ID = "id";

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        List<NodeRef> result = birthdaysUtils.search();
        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        objectMapper.writeValue(res.getOutputStream(), buildResponse(result));
        res.setStatus(Status.STATUS_OK);
    }

    private List<Map<String, String>> buildResponse(List<NodeRef> nodeRefs) {
        if (nodeRefs == null || nodeRefs.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<Map<String, String>> response = new ArrayList<>(nodeRefs.size());
        for (NodeRef nodeRef : nodeRefs) {
            response.add(getPersonInfo(nodeRef));
        }
        return response;
    }

    private Map<String, String> getPersonInfo(NodeRef nodeRef) {
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
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(date);
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setBirthdaysUtils(BirthdaysUtils birthdaysUtils) {
        this.birthdaysUtils = birthdaysUtils;
    }
}
