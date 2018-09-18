package ru.citeck.ecos.records.actions;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.action.group.*;
import ru.citeck.ecos.action.group.impl.ResultsListener;
import ru.citeck.ecos.records.RecordInfo;
import ru.citeck.ecos.records.RecordsService;
import ru.citeck.ecos.records.source.RecordsDAO;
import ru.citeck.ecos.records.source.RemoteRecordsDAO;
import ru.citeck.ecos.remote.RestConnection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class RecordsActionFactory<T> implements GroupActionFactory<RecordInfo<T>> {

    private static final String DEFAULT_GROUP_ACTION_METHOD = "alfresco/service/citeck/ecos/records-group-action";

    private String groupActionMethod = DEFAULT_GROUP_ACTION_METHOD;

    private RecordsService recordsService;

    @Override
    public final GroupAction<RecordInfo<T>> createAction(GroupActionConfig config) {
        RecordsGroupAction<T> localAction = createLocalAction(config);
        return new Action(localAction, config);
    }

    protected abstract RecordsGroupAction<T> createLocalAction(GroupActionConfig config);

    class Action implements GroupAction<RecordInfo<T>> {

        private GroupActionConfig localConfig;
        private RecordsGroupAction<T> localAction;

        private Map<String, Optional<GroupAction<RecordInfo<T>>>> actionsBySource = new ConcurrentHashMap<>();

        public Action(RecordsGroupAction<T> localAction, GroupActionConfig localConfig) {
            this.localConfig = localConfig;
            this.localAction = localAction;
        }

        @Override
        public void process(RecordInfo<T> node) {

            String sourceId = node.getRef().getSourceId();
            Optional<GroupAction<RecordInfo<T>>> optAction =
                    actionsBySource.computeIfAbsent(sourceId, this::getSourceAction);

            if (optAction.isPresent()) {

                GroupAction<RecordInfo<T>> action = optAction.get();
                List<RecordInfo<T>> withoutMetadata = new ArrayList<>();

                if (node.getData() != null) {
                    action.process(node);
                } else {
                    withoutMetadata.add(node);
                }

                if (withoutMetadata.size() > 0) {
                    onError(withoutMetadata, "Record metadata not found");
                }
            } else {
                onError(Collections.singletonList(node), "Records source " + sourceId + " not found!");
            }
        }

        private void onError(List<RecordInfo<T>> nodes, String message) {
            ActionStatus status = new ActionStatus(ActionStatus.STATUS_ERROR);
            status.setMessage(message);
            localAction.onProcessed(nodes.stream()
                                         .map(n -> new ActionResult<>(n, status))
                                         .collect(Collectors.toList()));
        }

        @Override
        public List<ActionResult<RecordInfo<T>>> complete() {
            actionsBySource.values().forEach(opt -> opt.ifPresent(GroupAction::complete));
            return localAction.complete();
        }

        @Override
        public List<ActionResult<RecordInfo<T>>> cancel() {
            actionsBySource.values().forEach(opt -> opt.ifPresent(GroupAction::cancel));
            return localAction.cancel();
        }

        @Override
        public boolean isAsync() {
            return localAction.isAsync();
        }

        @Override
        public long getTimeout() {
            return localAction.getTimeout();
        }

        @Override
        public void addListener(ResultsListener<RecordInfo<T>> listener) {
            localAction.addListener(listener);
        }

        private Optional<GroupAction<RecordInfo<T>>> getSourceAction(String id) {
            if (id.isEmpty()) {
                return Optional.of(localAction);
            }
            Optional<RecordsDAO> recordsDAO = recordsService.getRecordsSource(id);
            if (recordsDAO.isPresent()) {
                if (recordsDAO.get() instanceof RemoteRecordsDAO) {

                    GroupActionConfig remoteActionConfig = new GroupActionConfig();
                    remoteActionConfig.setBatchSize(localAction.getRemoteBatchSize());

                    GroupActionConfig targetConfig = new GroupActionConfig(localConfig);
                    targetConfig.setAsync(false);

                    RemoteRecordsDAO remoteDAO = (RemoteRecordsDAO) recordsDAO.get();
                    RestConnection restConn = remoteDAO.getRestConnection();

                    GroupAction<RecordInfo<T>> action = new RemoteGroupAction<>(remoteActionConfig,
                                                                                restConn,
                                                                                groupActionMethod,
                                                                                getActionId(),
                                                                                targetConfig);
                    action.addListener(localAction);
                    return Optional.of(action);
                } else {
                    return Optional.of(localAction);
                }
            }
            return Optional.empty();
        }

        @Override
        public String toString() {
            return localAction.toString();
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
            Action action = (Action) o;
            return localAction.equals(action.localAction);
        }

        @Override
        public int hashCode() {
            return localAction.hashCode();
        }
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
    }

    public void setGroupActionMethod(String groupActionMethod) {
        this.groupActionMethod = groupActionMethod;
    }
}

