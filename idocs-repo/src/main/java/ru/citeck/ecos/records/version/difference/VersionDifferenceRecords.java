package ru.citeck.ecos.records.version.difference;

import lombok.Data;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.request.query.RecordsQuery;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;
import ru.citeck.ecos.records2.source.dao.local.LocalRecordsDAO;
import ru.citeck.ecos.records2.source.dao.local.RecordsQueryWithMetaLocalDAO;
import ru.citeck.ecos.version.VersionDifferenceUtils;

import java.util.Collections;
import java.util.LinkedList;

/**
 * @author Roman Makarskiy
 */
@Component
public class VersionDifferenceRecords extends LocalRecordsDAO
        implements RecordsQueryWithMetaLocalDAO<VersionDifferenceDTO> {

    private static final String TEMPLATE = "<span class=\"%s\">%s</span>";
    private final VersionDifferenceUtils versionDifferenceUtils;

    @Autowired
    public VersionDifferenceRecords(VersionDifferenceUtils versionDifferenceUtils) {
        this.versionDifferenceUtils = versionDifferenceUtils;
    }

    private static final String ID = "version-diff";

    {
        setId(ID);
    }

    @Override
    public RecordsQueryResult<VersionDifferenceDTO> getMetaValues(RecordsQuery recordsQuery) {
        VersionDifferenceQuery query = recordsQuery.getQuery(VersionDifferenceQuery.class);

        checkRecordRefIsNodeRef(query.getFirst());
        checkRecordRefIsNodeRef(query.getSecond());

        NodeRef first = new NodeRef(query.getFirst().getId());
        NodeRef second = new NodeRef(query.getSecond().getId());

        LinkedList<String[]> diffObjList = versionDifferenceUtils.getDiff(first, second);

        StringBuilder diff = new StringBuilder();

        for (String[] s : diffObjList) {
            diff.append(String.format(TEMPLATE, s[0], s[1]));
        }

        VersionDifferenceDTO dto = new VersionDifferenceDTO();
        dto.setDiff(diff.toString());

        RecordsQueryResult<VersionDifferenceDTO> result = new RecordsQueryResult<>();
        result.setRecords(Collections.singletonList(dto));
        result.setTotalCount(1);

        return result;
    }

    private void checkRecordRefIsNodeRef(RecordRef recordRef) {
        if (recordRef == null || StringUtils.isBlank(recordRef.getId())) {
            throw new IllegalArgumentException("Mandatory parameter is null");
        }

        String id = recordRef.getId();
        if (!NodeRef.isNodeRef(id)) {
            throw new IllegalArgumentException("Record id should be NodeRef format");
        }
    }

    @Data
    public static class VersionDifferenceQuery {
        private RecordRef first;
        private RecordRef second;
    }
}
