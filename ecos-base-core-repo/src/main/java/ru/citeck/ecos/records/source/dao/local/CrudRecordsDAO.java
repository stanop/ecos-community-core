package ru.citeck.ecos.records.source.dao.local;

public abstract class CrudRecordsDAO<T> extends LocalRecordsDAO
                                        implements RecordsMetaLocalDAO<T>,
                                                   RecordsQueryWithMetaLocalDAO<T>,
                                                   MutableRecordsLocalDAO<T> {
}
