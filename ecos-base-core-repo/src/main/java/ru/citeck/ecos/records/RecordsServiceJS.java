package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import org.alfresco.repo.jscript.ValueConverter;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecordsServiceJS extends AlfrescoScopableProcessorExtension {

    private RecordsService recordsService;

    private static ValueConverter converter = new ValueConverter();
    private static ObjectMapper objectMapper = new ObjectMapper();

    public ActionResult<RecordRef>[] executeAction(Object nodes,
                                                   String actionId,
                                                   Object config) {

        Iterable<RecordRef> nodeRefs = toIterableNodes(nodes);
        GroupActionConfig actionConfig = convertConfig(config, GroupActionConfig.class);
        return toArray(recordsService.executeAction(nodeRefs, actionId, actionConfig));
    }

    public ActionResult<RecordRef>[] executeAction(String sourceId,
                                                   Object recordsQuery,
                                                   String actionId,
                                                   Object config) {

        RecordsQuery convertedQuery = convertConfig(recordsQuery, RecordsQuery.class);
        GroupActionConfig actionConfig = convertConfig(config, GroupActionConfig.class);
        return toArray(recordsService.executeAction(sourceId, convertedQuery, actionId, actionConfig));
    }

    private static Iterable<RecordRef> toIterableNodes(Object nodes) {
        Object jNodes = converter.convertValueForJava(nodes);
        List<RecordRef> resultList;
        if (jNodes instanceof List) {
            resultList = new ArrayList<>();
            for (Object obj : (List) jNodes) {
                resultList.add(JavaScriptImplUtils.getRecordRef(obj));
            }
        } else if (jNodes instanceof JSONArray) {
            resultList = new ArrayList<>();
            JSONArray jsonArray = (JSONArray) jNodes;
            for (int i = 0; i < jsonArray.length(); i++) {
                resultList.add(JavaScriptImplUtils.getRecordRef(jsonArray.opt(i)));
            }
        } else if (jNodes instanceof Iterable) {
            @SuppressWarnings("unchecked")
            Iterable<ru.citeck.ecos.records.RecordRef> iterableNodes = (Iterable<RecordRef>) jNodes;
            return Iterables.transform(iterableNodes, JavaScriptImplUtils::getRecordRef);
        } else {
            resultList = Collections.emptyList();
        }
        return resultList;
    }

    private static <T> ActionResult<T>[] toArray(List<ActionResult<T>> results) {
        @SuppressWarnings("unchecked")
        ActionResult<T>[] result = new ActionResult[results.size()];
        return results.toArray(result);
    }

    private static <T> T convertConfig(Object config, Class<T> type) {
        if (config == null) {
            return null;
        }
        Object configObj = converter.convertValueForJava(config);
        return objectMapper.convertValue(configObj, type);
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
    }
}
