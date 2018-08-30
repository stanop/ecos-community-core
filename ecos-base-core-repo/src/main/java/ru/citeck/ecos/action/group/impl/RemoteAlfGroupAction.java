package ru.citeck.ecos.action.group.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.GroupActionConfig;

import java.util.List;

public class RemoteAlfGroupAction extends BaseGroupAction<String> {

    private static final String GROUP_ACTION_URL = "api/journals/group-action";

    private RestTemplate restTemplate;

    private String targetAction;
    private GroupActionConfig targetConfig;

    private ObjectMapper objectMapper = new ObjectMapper();

    private final String url;

    public RemoteAlfGroupAction(GroupActionConfig config,
                                String serviceUrl,
                                String targetAction,
                                GroupActionConfig targetConfig,
                                RestTemplate restTemplate) {
        super(config);
        this.url = serviceUrl + GROUP_ACTION_URL;
        this.restTemplate = restTemplate;
        this.targetAction = targetAction;
        this.targetConfig = targetConfig;
    }

    @Override
    protected void processNodesImpl(List<String> nodes, List<ActionResult<String>> output) {

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        ActionData data = new ActionData();
        data.actionId = targetAction;
        data.config = targetConfig;

        try {
            map.add("payload", objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Data can't be converted to json", e);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<Response> result = restTemplate.exchange(url,
                                                                HttpMethod.POST,
                                                                request,
                                                                Response.class);
        output.addAll(result.getBody().results);
    }

    private static class ActionData {
        public String actionId;
        public GroupActionConfig config;
    }

    private static class Response {
        public List<ActionResult<String>> results;
    }
}
