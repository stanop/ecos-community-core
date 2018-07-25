package ru.citeck.ecos.journals.module;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.utils.NodeUtils;

import java.util.Optional;

public class AddToJournalListModuleComponent extends AbstractModuleComponent {

    private NodeUtils nodeUtils;
    private SearchService searchService;

    private String journalList;
    private String journal;

    @Override
    protected void checkProperties() {
        super.checkProperties();
        PropertyCheck.mandatory(this, "journalList", journalList);
        PropertyCheck.mandatory(this, "journal", journal);
    }

    @Override
    protected void executeInternal() throws Throwable {

        Optional<NodeRef> listRef = FTSQuery.create()
                .type(JournalsModel.TYPE_JOURNALS_LIST).and()
                .exact(ContentModel.PROP_NAME, journalList)
                .transactional()
                .queryOne(searchService);

        if (!listRef.isPresent()) {
            throw new IllegalArgumentException("Journal list with id " + journalList + " not found");
        }

        Optional<NodeRef> journalRef = FTSQuery.create()
                .type(JournalsModel.TYPE_JOURNAL).and()
                .exact(JournalsModel.PROP_JOURNAL_TYPE, journal)
                .transactional()
                .queryOne(searchService);

        if (!journalRef.isPresent()) {
            throw new IllegalArgumentException("Journal with id " + journal + " not found");
        }

        nodeUtils.createAssoc(listRef.get(), journalRef.get(), JournalsModel.ASSOC_JOURNALS);
    }

    @Autowired
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Autowired
    public void setNodeUtils(NodeUtils nodeUtils) {
        this.nodeUtils = nodeUtils;
    }

    public void setJournalList(String journalList) {
        this.journalList = journalList;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }
}
