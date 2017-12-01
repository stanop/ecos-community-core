package ru.citeck.ecos.search.ftsquery;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;

import java.util.List;

public interface OperatorExpected {

    OperandExpected or();
    OperandExpected and();
    NodeRef queryOne(SearchService searchService);
    List<NodeRef> query(SearchService searchService);
    OperatorExpected end();

    OperatorExpected transactional();

}