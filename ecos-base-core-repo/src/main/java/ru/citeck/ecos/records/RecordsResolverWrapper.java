package ru.citeck.ecos.records;

import ru.citeck.ecos.records2.resolver.RecordsResolver;

public interface RecordsResolverWrapper extends RecordsResolver {
    void setRecordsResolver(RecordsResolver recordsResolver);
}
