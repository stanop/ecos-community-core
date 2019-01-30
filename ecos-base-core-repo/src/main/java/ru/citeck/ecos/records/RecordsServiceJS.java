package ru.citeck.ecos.records;

import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.result.RecordsResult;
import ru.citeck.ecos.records.rest.RecordsQueryPost;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JsUtils;

import java.util.*;

public class RecordsServiceJS extends AlfrescoScopableProcessorExtension {

    @Autowired
    private RecordsService recordsService;
    @Autowired
    private RecordsQueryPost recordsQueryPost;

    private JsUtils jsUtils;

    public ActionResult<RecordRef>[] executeAction(Object nodes,
                                                   Object config) {

        RecordsList records = jsUtils.toJava(nodes, RecordsList.class);
        GroupActionConfig actionConfig = jsUtils.toJava(config, GroupActionConfig.class);

        return toArray(recordsService.executeAction(records, actionConfig));
    }

    public RecordsResult<RecordMeta> getAttributes(Object records, Object attributes) {

        ParameterCheck.mandatory("records", records);
        ParameterCheck.mandatory("attributes", attributes);

        RecordsList javaRecords = jsUtils.toJava(records, RecordsList.class);
        Object javaAttributes = jsUtils.toJava(attributes);

        if (javaAttributes instanceof Collection) {
            return recordsService.getAttributes(javaRecords, (Collection<String>) javaAttributes);
        } else if (javaAttributes instanceof Map) {
            return recordsService.getAttributes(javaRecords, (Map<String, String>) javaAttributes);
        }
        throw new IllegalArgumentException("Attributes type is not supported! " +
                                           javaAttributes.getClass() + " " + attributes.getClass());
    }

    public RecordsResult<?> getRecords(Object recordsQuery) {
        RecordsQueryPost.Request request = jsUtils.toJava(recordsQuery, RecordsQueryPost.Request.class);
        return recordsQueryPost.queryRecords(request);
    }

    public <T> RecordsResult<T> getRecords(Object recordsQuery, Class<T> schemaClass) {
        RecordsQuery convertedQuery = jsUtils.toJava(recordsQuery, RecordsQuery.class);
        return recordsService.getRecords(convertedQuery, schemaClass);
    }

    public Iterable<RecordRef> getIterableRecords(Object recordsQuery) {
        RecordsQuery convertedQuery = jsUtils.toJava(recordsQuery, RecordsQuery.class);
        return recordsService.getIterableRecords(convertedQuery);
    }

    private static <T> ActionResult<T>[] toArray(ActionResults<T> results) {
        @SuppressWarnings("unchecked")
        ActionResult<T>[] result = new ActionResult[results.getResults().size()];
        return results.getResults().toArray(result);
    }

    @Autowired
    public void setJsUtils(JsUtils jsUtils) {
        this.jsUtils = jsUtils;
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
    }

    public static class RecordsList extends ArrayList<RecordRef> {}
}
