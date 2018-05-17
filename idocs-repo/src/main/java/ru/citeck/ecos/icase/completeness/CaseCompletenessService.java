package ru.citeck.ecos.icase.completeness;

import java.util.Collection;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

public interface CaseCompletenessService {

    /**
     * Get all completeness levels, available for the specified case.
     */
    Set<NodeRef> getAllLevels(NodeRef caseNode);
    
    /**
     * Get all completeness levels, that are completed for the specified case.
     */
    Set<NodeRef> getCompletedLevels(NodeRef caseNode);

    /**
     * Get requirement nodes for the specified level.
     */
    Set<NodeRef> getLevelRequirements(NodeRef levelRef);

    /**
     * Check that level is completed
     */
    boolean isLevelCompleted(NodeRef caseRef, NodeRef levelRef);

    /**
     * Check that all specified levels is completed
     */
    boolean isLevelsCompleted(NodeRef caseRef, Collection<NodeRef> levels);

    /**
     * Check that requirement is passed
     */
    boolean isRequirementPassed(NodeRef caseNode, NodeRef requirement);

    /**
     * Get requirement matched elements
     */
    Set<NodeRef> getRequirementMatchedElements(NodeRef caseRef, NodeRef requirement);

    /**
     * Get current levels for the specified case.
     */
    Set<NodeRef> getCurrentLevels(NodeRef caseNode);
    
}
