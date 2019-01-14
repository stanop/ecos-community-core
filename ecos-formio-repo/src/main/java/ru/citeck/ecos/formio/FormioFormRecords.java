package ru.citeck.ecos.formio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsQueryResult;
import ru.citeck.ecos.records.source.LocalRecordsDAO;
import ru.citeck.ecos.records.source.RecordsWithMetaDAO;

import java.util.Collections;
import java.util.Optional;

@Component
public class FormioFormRecords extends LocalRecordsDAO implements RecordsWithMetaDAO {

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
