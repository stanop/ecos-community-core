package ru.citeck.ecos.eform;

import lombok.Getter;
import lombok.Setter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.content.ContentData;
import ru.citeck.ecos.eform.model.EcosFormModel;
import ru.citeck.ecos.eform.provider.RepoFormProvider;
import ru.citeck.ecos.records.RecordMeta;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.request.delete.RecordsDelResult;
import ru.citeck.ecos.records.request.delete.RecordsDeletion;
import ru.citeck.ecos.records.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records.request.mutation.RecordsMutation;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsQueryResult;
import ru.citeck.ecos.records.source.LocalRecordsDAO;
import ru.citeck.ecos.records.source.MutableRecordsDAO;
import ru.citeck.ecos.records.source.RecordsMetaDAO;
import ru.citeck.ecos.records.source.RecordsWithMetaDAO;
import ru.citeck.ecos.records.source.alfnode.AlfNodesRecordsDAO;
import ru.citeck.ecos.records.source.alfnode.search.CriteriaAlfNodesSearch;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class EcosFormRecords extends LocalRecordsDAO
                               implements RecordsMetaDAO,
                                          RecordsWithMetaDAO,
                                          MutableRecordsDAO {

    public static final String ID = "eform";

    private static final String ECOS_FORM_KEY = "ECOS_FORM";

    private static final String ATT_TITLE = "title";
    private static final String ATT_FORM_KEY = "formKey";
    private static final String ATT_DEFINITION = "definition";
    private static final String ATT_DESCRIPTION = "description";
    private static final String ATT_CUSTOM_MODULE = "customModule";

    private AlfNodesRecordsDAO alfNodesRecordsDAO;
    private EcosFormService eformFormService;

    private RepoFormProvider repoFormProvider;

    @Autowired
    public EcosFormRecords(EcosFormService eformFormService,
                           AlfNodesRecordsDAO alfNodesRecordsDAO,
                           RepoFormProvider repoFormProvider) {
        setId(ID);
        this.repoFormProvider = repoFormProvider;
        this.eformFormService = eformFormService;
        this.alfNodesRecordsDAO = alfNodesRecordsDAO;
    }

    @Override
    public RecordsMutResult mutate(RecordsMutation mutation) {

        RecordsMutResult result = new RecordsMutResult();

        for (RecordMeta record : mutation.getRecords()) {

            if (record.getId().getId().isEmpty()) {

                String formKey = record.getAttribute("formKey", "");

                if (StringUtils.isBlank(formKey)) {
                    throw new RuntimeException("Form key can't be null");
                }

                EcosFormModel model = new EcosFormModel();

                fillModel(model, record);

                result.addRecord(new RecordMeta(eformFormService.save(model)));

            } else {

                Optional<EcosFormModel> model = eformFormService.getFormById(record.getId().getId());
                model.ifPresent(m -> {

                    EcosFormModel changedModel = new EcosFormModel(m);
                    fillModel(changedModel, record);

                    result.addRecord(new RecordMeta(eformFormService.save(changedModel)));
                });
            }
        }

        return result;
    }

    private void fillModel(EcosFormModel model, RecordMeta record) {
        if (record.hasAttribute(ATT_FORM_KEY)) {
            model.setFormKey(record.getAttribute(ATT_FORM_KEY, ""));
        }
        if (record.hasAttribute(ATT_DEFINITION)) {
            model.setDefinition(record.getAttribute(ATT_DEFINITION));
        }
        if (record.hasAttribute(ATT_DESCRIPTION)) {
            model.setDescription(record.getAttribute(ATT_DESCRIPTION, ""));
        }
        if (record.hasAttribute(ATT_TITLE)) {
            model.setTitle(record.getAttribute(ATT_TITLE, ""));
        }
        if (record.hasAttribute(ATT_CUSTOM_MODULE)) {
            model.setCustomModule(record.getAttribute(ATT_CUSTOM_MODULE, ""));
        }
    }

    @Override
    public RecordsDelResult delete(RecordsDeletion deletion) {
        return new RecordsDelResult();
    }

    @Override
    protected List<EcosFormModel> getMetaValues(List<RecordRef> records) {

        List<EcosFormModel> models = new ArrayList<>();

        for (RecordRef recordRef : records) {

            if (NodeRef.isNodeRef(recordRef.getId())) {

                Optional<EcosFormModel> model = repoFormProvider.getContentData(
                                new NodeRef(recordRef.getId())).flatMap(ContentData::getData);
                model.ifPresent(models::add);

            } else {

                if (recordRef.getId().isEmpty()) {

                    EcosFormModel form = new EcosFormModel();
                    form.setId("");
                    models.add(form);

                } else {

                    EcosFormModel form = repoFormProvider.getFormById(recordRef.getId());
                    if (form != null) {
                        models.add(form);
                    }
                }
            }
        }

        return models;
    }

    @Override
    protected RecordsQueryResult<EcosFormModel> getMetaValues(RecordsQuery recordsQuery) {

        String lang = recordsQuery.getLanguage();
        RecordsQueryResult<EcosFormModel> result = new RecordsQueryResult<>();

        if (lang.equals(SearchService.LANGUAGE_FTS_ALFRESCO) || lang.equals(CriteriaAlfNodesSearch.LANGUAGE)) {

            RecordsQueryResult<RecordRef> records = alfNodesRecordsDAO.getRecords(recordsQuery);

            result.setHasMore(records.getHasMore());
            result.setTotalCount(records.getTotalCount());
            result.setRecords(getMetaValues(records.getRecords()));

            return result;
        }

        Query query = recordsQuery.getQuery(Query.class);
        Optional<EcosFormModel> form = Optional.empty();

        if (StringUtils.isNotBlank(query.formKey)) {

            form = eformFormService.getFormByKey(Arrays.stream(query.formKey.split(","))
                                                        .filter(StringUtils::isNotBlank)
                                                        .collect(Collectors.toList()));

        } else if (query.record != null) {

            if (ID.equals(query.record.getSourceId())) {

                form = eformFormService.getFormByKey(ECOS_FORM_KEY);

            } else {
                form = eformFormService.getFormByRecord(query.record, query.isViewMode);
            }
        }

        if (!form.isPresent()) {
            return result;
        }

        result.setRecords(Collections.singletonList(form.get()));
        result.setTotalCount(1);

        return result;
    }

    static class Query {
        @Getter @Setter private String formKey;
        @Getter @Setter private RecordRef record;
        @Getter @Setter private Boolean isViewMode;
    }
}
