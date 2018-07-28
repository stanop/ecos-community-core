package ru.citeck.ecos.journals.action.group;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.journals.records.IterableJournalRecords;
import ru.citeck.ecos.journals.records.JournalRecordsDAO;
import ru.citeck.ecos.repo.RemoteNodeRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Pavel Simonov
 */
public class GroupActionServiceImpl implements GroupActionService {

    private static final long PROCESS_TIMEOUT_SEC = 60 * 5;

    private static Log logger = LogFactory.getLog(GroupActionServiceImpl.class);

    private JournalRecordsDAO journalRecordsDAO;

    private Map<String, GroupActionProcFactory> processorFactories = new HashMap<>();

    @Autowired
    public GroupActionServiceImpl(JournalRecordsDAO journalRecordsDAO) {
        this.journalRecordsDAO = journalRecordsDAO;
    }

    @Override
    public Map<RemoteNodeRef, GroupActionResult> invoke(List<RemoteNodeRef> nodeRefs,
                                                        String actionId,
                                                        Map<String, String> params) {

        GroupActionProcessor processor = getProcessor(actionId, params);

        Map<RemoteNodeRef, Future<GroupActionResult>> futureResults = new HashMap<>();

        for (RemoteNodeRef ref : nodeRefs) {
            futureResults.put(ref, processor.process(ref));
        }

        Map<RemoteNodeRef, GroupActionResult> results = new HashMap<>();

        futureResults.forEach((ref, res) -> {

            GroupActionResult result;
            try {
                result = res.get(PROCESS_TIMEOUT_SEC, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                throw new RuntimeException("Processing result get timeout exception. " +
                                           "Time: " + PROCESS_TIMEOUT_SEC + " seconds. " +
                                           "Node: " + ref, e);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Exception while processing result get. Node: " + ref, e);
            }
            results.put(ref, result);
        });

        return results;
    }

    @Override
    public void invoke(String query,
                       String journalId,
                       String language,
                       String actionId,
                       Map<String, String> params) {

        GroupActionProcessor processor = getProcessor(actionId, params);

        IterableJournalRecords records = new IterableJournalRecords(journalRecordsDAO, query, journalId, language);

        for (RemoteNodeRef nodeRef : records) {
            processor.process(nodeRef);
        }
    }

    private GroupActionProcessor getProcessor(String actionId, Map<String, String> params) {

        GroupActionProcFactory factory = processorFactories.get(actionId);
        if (factory == null) {
            throw new IllegalArgumentException("Action not found: '" + actionId + "'");
        }

        checkParams(params, factory.getMandatoryParams());

        return factory.createProcessor(params);
    }

    private void checkParams(Map<String, String> params, String[] mandatoryParams) {
        List<String> missing = new ArrayList<>(mandatoryParams.length);
        for (String param : mandatoryParams) {
            if (!params.containsKey(param) || StringUtils.isBlank(params.get(param))) {
                missing.add(param);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Mandatory parameters are missing: " + String.join(", ", missing));
        }
    }

    @Override
    public void register(GroupActionProcFactory factory) {
        processorFactories.put(factory.getActionId(), factory);
    }
}
