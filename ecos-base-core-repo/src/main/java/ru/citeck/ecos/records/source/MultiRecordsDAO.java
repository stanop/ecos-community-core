package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import ru.citeck.ecos.action.group.GroupAction;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.AttributeInfo;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.query.DaoRecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Pavel Simonov
 */
public class MultiRecordsDAO extends AbstractRecordsDAO {

    private List<RecordsDAO> recordsDao;

    public MultiRecordsDAO(String id) {
        super(id);
    }

    @Override
    public DaoRecordsResult queryRecords(RecordsQuery query) {
        int startIdx = 0;
        if (query.isAfterIdMode()) {
            String afterId = query.getAfterId();
            if (StringUtils.isNotBlank(afterId)) {
                RecordRef afterRecordRef = new RecordRef(afterId);
                String source = afterRecordRef.getSourceId();
                while (startIdx < recordsDao.size() && !recordsDao.get(startIdx).getId().equals(source)) {
                    startIdx++;
                }
                query = new RecordsQuery(query);
                query.setAfterId(afterRecordRef.getId());
            }
        }
        

        for (int i = startIdx; i < recordsDao.size(); i++) {

        }

        return null;
    }

    @Override
    public Map<String, ObjectNode> queryMeta(Collection<String> records, String gqlSchema) {
        return null;
    }

    @Override
    public <V> Map<String, V> queryMeta(Collection<String> records, Class<V> metaClass) {
        return null;
    }

    @Override
    public Optional<AttributeInfo> getAttributeInfo(String name) {
        return null;
    }

    @Override
    public Optional<MetaValue> getMetaValue(GqlContext context, String id) {
        return null;
    }

    @Override
    public GroupAction<String> createAction(String actionId, GroupActionConfig config) {

        return null;
    }

    public void setRecordsDao(List<RecordsDAO> records) {
        this.recordsDao = records;
    }
}
