package ru.citeck.ecos.job.actions;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Implements for specify processing logic for {@code ExecuteActionByDateWork}
 *
 * @author Roman Makarskiy
 */
interface ActionProcessor {

    /**
     * Process each entry
     *
     * @param entry NodeRef of entry for processing
     */
    void process(NodeRef entry);
}
