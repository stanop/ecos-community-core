package ru.citeck.ecos.action.group.impl;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.GroupActionPost;

import java.util.List;
import java.util.Objects;

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

        headers.setContentType(MediaType.APPLICATION_JSON);

        GroupActionPost.ActionData data = new GroupActionPost.ActionData();
        data.actionId = targetAction;
        data.config = targetConfig;
        data.records = nodes;

        HttpEntity<GroupActionPost.ActionData> request = new HttpEntity<>(data, headers);

        ResponseEntity<GroupActionPost.Response> result = restTemplate.exchange(groupActionUrl,
                                                                HttpMethod.POST,
                                                                request,
                                                                GroupActionPost.Response.class);
        output.addAll(result.getBody().results);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        RemoteGroupAction that = (RemoteGroupAction) o;

        return Objects.equals(targetAction, that.targetAction) &&
               Objects.equals(targetConfig, that.targetConfig) &&
               Objects.equals(groupActionUrl, that.groupActionUrl);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(targetAction);
        result = 31 * result + Objects.hashCode(targetConfig);
        result = 31 * result + Objects.hashCode(groupActionUrl);
        return result;
    }
}
