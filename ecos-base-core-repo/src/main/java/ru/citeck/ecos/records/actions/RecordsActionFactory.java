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

public abstract class RecordsActionFactory<T, LocalAction extends GroupAction<RecordInfo<T>>>
                                                          implements GroupActionFactory<RecordInfo<T>> {

    public static final String DEFAULT_GROUP_ACTION_METHOD = "alfresco/service/citeck/ecos/records-group-action";

    private RecordsService recordsService;

    @Override
    public final GroupAction<RecordInfo<T>> createAction(GroupActionConfig config) {
        return new Action(createLocalAction(config));
    }

    protected abstract LocalAction createLocalAction(GroupActionConfig config);

    protected GroupAction<RecordInfo<T>> createRemoteAction(LocalAction localAction, RestConnection restConn) {
        return null;
    }

    class Action implements GroupAction<RecordInfo<T>> {

        private LocalAction localAction;

        private Map<String, Optional<GroupAction<RecordInfo<T>>>> actionsBySource = new ConcurrentHashMap<>();

        public Action(LocalAction localAction) {
            this.localAction = localAction;
        }

        @Override
        public void process(RecordInfo<T> node) {

            String sourceId = node.getRef().getSourceId();
            Optional<GroupAction<RecordInfo<T>>> optAction =
                    actionsBySource.computeIfAbsent(sourceId, this::getSourceAction);

            if (optAction.isPresent()) {
                optAction.get().process(node);
            } else {
                setStatus(Collections.singletonList(node),
                        "Records source " + sourceId + " is not exists or not supported!");
            }
        }

        private void setStatus(List<RecordInfo<T>> nodes, String message) {
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

                    RemoteRecordsDAO remoteDAO = (RemoteRecordsDAO) recordsDAO.get();
                    RestConnection restConn = remoteDAO.getRestConnection();

                    return Optional.ofNullable(createRemoteAction(localAction, restConn));
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
}

