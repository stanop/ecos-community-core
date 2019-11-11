package ru.citeck.ecos.records.processor;

import ru.citeck.ecos.search.ftsquery.FTSQuery;

/*
*   Interface for declare methods to process some type of predicate
*
*   T - Predicate class
*
* */
public interface PredicateProcessor<T> {

    void process(T predicate, FTSQuery query);
}
