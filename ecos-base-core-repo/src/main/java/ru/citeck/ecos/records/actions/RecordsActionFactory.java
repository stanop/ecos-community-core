package ru.citeck.ecos.records.actions;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.action.group.*;
import ru.citeck.ecos.action.group.impl.BaseGroupAction;
import ru.citeck.ecos.records.RecordInfo;
import ru.citeck.ecos.records.RecordsService;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records.source.RecordsDAO;
import ru.citeck.ecos.records.source.RemoteRecordsDAO;
import ru.citeck.ecos.remote.RestConnection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class RecordsActionFactory<T> implements GroupActionFactory<RecordInfo<T>> {

    private static final String DEFAULT_GROUP_ACTION_METHOD = "citeck/ecos/group-action";

    private String groupActionMethod = DEFAULT_GROUP_ACTION_METHOD;

    private RecordsService recordsService;

    @Override
    public final GroupAction<RecordInfo<T>> createAction(GroupActionConfig config) {

        RecordsGroupAction<T> localAction = createLocalAction(config);

        return new Action(localAction, new GroupActionConfig());
    }

    protected abstract RecordsGroupAction<T> createLocalAction(GroupActionConfig config);

    class Action extends BaseGroupAction<RecordInfo<T>> {

        private RecordsGroupAction<T> localAction;
        private Map<String, Optional<GroupAction<RecordInfo<T>>>> actionsBySource = new ConcurrentHashMap<>();

        public Action(RecordsGroupAction<T> localAction, GroupActionConfig config) {
            super(config);
            this.localAction = localAction;
        }

        @Override
        protected void processNodesImpl(List<RecordInfo<T>> nodes) {

            Map<String, Set<RecordInfo<T>>> infoBySource = RecordsUtils.groupInfoBySource(nodes);

            infoBySource.forEach((id, infoSet) -> {

                Optional<GroupAction<RecordInfo<T>>> optAction =
                        actionsBySource.computeIfAbsent(id, this::getSourceAction);

                if (optAction.isPresent()) {

                    GroupAction<RecordInfo<T>> action = optAction.get();
                    List<RecordInfo<T>> withoutMetadata = new ArrayList<>();

                    for (RecordInfo<T> info : infoSet) {
                        if (info.getData() != null) {
                            action.process(info);
                        } else {
                            withoutMetadata.add(info);
                        }
                    }

                    onError(withoutMetadata, "Record metadata not found");
                } else {
                    onError(nodes, "Records source " + id + " not found!");
                }
            });
        }

        private void onError(List<RecordInfo<T>> nodes, String message) {
            ActionStatus status = new ActionStatus(ActionStatus.STATUS_ERROR);
            status.setMessage(message);
            localAction.onProcessed(nodes.stream()
                                         .map(n -> new ActionResult<>(n, status))
                                         .collect(Collectors.toList()));
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

                    RemoteRecordsDAO remoteDAO = (RemoteRecordsDAO) recordsDAO.get();
                    RestConnection restConn = remoteDAO.getRestConnection();

                    GroupAction<RecordInfo<T>> action = new RemoteGroupAction<>(remoteActionConfig,
                                                                                restConn,
                                                                                groupActionMethod,
                                                                                getActionId(),
                                                                                config);
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

