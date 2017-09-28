package ru.citeck.ecos.icase.levels.api;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Set;

/**
 * @author Alexander Nemerov
 *         created on 17.03.2015.
 */
public interface CompleteLevelsChecker
        extends CompleteLevelsCheckerGeneric<NodeRef, Set<NodeRef>> {
}
