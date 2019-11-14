package ru.citeck.ecos.records.processor.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.predicate.model.EmptyPredicate;
import ru.citeck.ecos.records.processor.PredicateProcessor;
import ru.citeck.ecos.records.processor.utils.ProcessorUtils;
import ru.citeck.ecos.search.ftsquery.FTSQuery;

@Component
public class EmptyPredicateProcessor implements PredicateProcessor<EmptyPredicate> {

    private ProcessorUtils processorUtils;

    @Override
    public void process(EmptyPredicate predicate, FTSQuery query) {
        String attribute = predicate.getAttribute();
        processorUtils.consumeQueryField(attribute, query::empty);
    }

    @Autowired
    public void setProcessorUtils(ProcessorUtils processorUtils) {
        this.processorUtils = processorUtils;
    }

}
