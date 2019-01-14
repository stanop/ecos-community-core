package ru.citeck.ecos.formio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.request.delete.RecordsDelResult;
import ru.citeck.ecos.records.request.delete.RecordsDeletion;
import ru.citeck.ecos.records.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records.request.mutation.RecordsMutation;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsQueryResult;
import ru.citeck.ecos.records.source.LocalRecordsDAO;
import ru.citeck.ecos.records.source.MutableRecordsDAO;
import ru.citeck.ecos.records.source.RecordsWithMetaDAO;

import java.util.Collections;
import java.util.Optional;

@Component
public class FormioFormRecords extends LocalRecordsDAO implements RecordsWithMetaDAO, MutableRecordsDAO {

    public static final String ID = "formio";

    private FormioFormService formioFormService;

    public FormioFormRecords() {
        setId(ID);
    }

    @Override
    protected RecordsQueryResult<?> getMetaValues(RecordsQuery recordsQuery) {

        Query query = recordsQuery.getQuery(Query.class);
        RecordsQueryResult<FormioForm> result = new RecordsQueryResult<>();

        Optional<FormioForm> form = formioFormService.getForm(query.formType,
                                                              query.formKey,
                                                              query.formId,
                                                              query.formMode);

        form.ifPresent(formioForm -> result.setRecords(Collections.singletonList(formioForm)));
        result.setHasMore(false);
        result.setTotalCount(form.isPresent() ? 1 : 0);

        return result;
    }

    @Override
    public RecordsMutResult mutate(RecordsMutation mutation) {
/*
        for (RecordMut record : mutation.getRecords()) {

            formioFormService.
            Optional<FormioForm> form = formioFormService.getForm(record.getId());


        }*/




        return null;
    }

    @Override
    public RecordsDelResult delete(RecordsDeletion deletion) {
        return null;
    }

    @Autowired
    public void setFormioFormService(FormioFormService formioFormService) {
        this.formioFormService = formioFormService;
    }

    static class Query {
        public String formType;
        public String formKey;
        public String formId;
        public String formMode;
    }
}
