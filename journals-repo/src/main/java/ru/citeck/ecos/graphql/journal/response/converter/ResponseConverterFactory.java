package ru.citeck.ecos.graphql.journal.response.converter;

import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.response.converter.impl.DefaultResponseConverter;
import ru.citeck.ecos.graphql.journal.response.converter.impl.SplitLoadingResponseConverter;

public class ResponseConverterFactory {

    public ResponseConverter getConverter(JournalDataSource dataSource) {
        if (dataSource.isSupportsSplitLoading()) {
            return new SplitLoadingResponseConverter();
        } else {
            return new DefaultResponseConverter();
        }
    }

}
