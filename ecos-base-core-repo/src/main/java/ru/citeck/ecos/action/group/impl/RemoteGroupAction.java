package ru.citeck.ecos.action.group.impl;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.GroupActionConfig;

import java.util.List;

public class RemoteGroupAction extends BaseGroupAction<String> {

    private final RestTemplate restTemplate;

    private final String targetAction;
    private final GroupActionConfig targetConfig;

    private final String groupActionUrl;

    public RemoteGroupAction(GroupActionConfig config,
                             RestTemplate restTemplate,
                             String groupActionUrl,
                             String targetAction,
                             GroupActionConfig targetConfig) {
        super(config);
        this.groupActionUrl = groupActionUrl;
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
        data.records = nodes;

        /*try {
            map.add("payload", objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Data can't be converted to json", e);
        }*/

        HttpEntity<ActionData> request = new HttpEntity<>(data, headers);

        ResponseEntity<Response> result = restTemplate.exchange(groupActionUrl,
                                                                HttpMethod.POST,
                                                                request,
                                                                Response.class);
        output.addAll(result.getBody().results);
    }

    private static class ActionData {
        public String actionId;
        public GroupActionConfig config;
        public List<String> records;
    }

    private static class Response {
        public List<ActionResult<String>> results;
    }
}
