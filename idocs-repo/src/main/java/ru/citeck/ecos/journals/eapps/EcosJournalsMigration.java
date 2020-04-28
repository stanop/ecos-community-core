package ru.citeck.ecos.journals.eapps;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.apps.app.provider.ComputedMeta;
import ru.citeck.ecos.apps.app.provider.ComputedModule;
import ru.citeck.ecos.commons.data.ObjectData;
import ru.citeck.ecos.commons.json.Json;
import ru.citeck.ecos.eapps.ModuleMigration;
import ru.citeck.ecos.journals.JournalService;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.journals.domain.JournalMeta;
import ru.citeck.ecos.journals.domain.JournalTypeColumn;
import ru.citeck.ecos.journals.eapps.dto.JournalModule;
import ru.citeck.ecos.journals.service.JournalColumnService;
import ru.citeck.ecos.journals.service.JournalMetaService;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.utils.NodeUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
//@Component
public class EcosJournalsMigration implements ModuleMigration {

    private static final int BATCH_SIZE = 100;

    private JournalService journalService;
    private JournalColumnService journalColumnService;
    private JournalMetaService journalMetaService;
    private NodeUtils nodeUtils;
    private SearchService searchService;

    @Autowired
    public EcosJournalsMigration(ServiceRegistry serviceRegistry,
                                 JournalService journalService,
                                 JournalColumnService journalColumnService,
                                 JournalMetaService journalMetaService,
                                 NodeUtils nodeUtils) {
        this.journalService = journalService;
        this.journalColumnService = journalColumnService;
        this.journalMetaService = journalMetaService;
        this.nodeUtils = nodeUtils;
        this.searchService = serviceRegistry.getSearchService();
    }

    @Override
    public List<ComputedModule> getModulesSince(long time) {

        Date receivedDate = new Date(time);

        List<NodeRef> journalsNodeRefs = FTSQuery.create().type(JournalsModel.TYPE_JOURNAL)
            .and()
            .rangeFrom(ContentModel.PROP_MODIFIED, receivedDate)
            .and()
            .maxItems(BATCH_SIZE)
            .addSort(ContentModel.PROP_MODIFIED, true)
            .eventual()
            .query(this.searchService);

        log.info("Journals found to export: " + journalsNodeRefs.size());

        List<JournalModule> modules = journalsNodeRefs.stream()
            .map(this::getJournalModule)
            .collect(Collectors.toList());

        return modules.stream()
            .map(j -> {
                Date cmModified = nodeUtils.getProperty(new NodeRef("workspace://SpacesStore/" + j.getId()),
                    ContentModel.PROP_MODIFIED);
                ComputedMeta moduleMeta = new ComputedMeta(j.getId(), cmModified.getTime());
                return new ComputedModule(j, moduleMeta);
            })
            .collect(Collectors.toList());
    }

    private JournalModule getJournalModule(NodeRef journalNodeRef) {

        JournalMeta journalMeta = journalMetaService.getJournalMeta(journalNodeRef);
        JournalType journalType = journalService.getJournalType(journalNodeRef);
        List<JournalTypeColumn> columns =
            journalColumnService.getJournalTypeColumns(journalType, journalMeta.getMetaRecord());

        return JournalModule.builder()
            .id(journalNodeRef.getId())
            .metaRecord(RecordRef.valueOf(journalMeta.getMetaRecord()))
            .name(nodeUtils.getDisplayName(journalNodeRef))
            .predicate(new ObjectData(journalType.getPredicate()))
            .actions(journalType.getActions())
            .columnsJSONStr(Json.getMapper().toJson(columns).toString())
            .attributes(journalType.getOptions())
            .build();
    }

    @Override
    public String getModuleType() {
        return "ui/old_journal";
    }
}
