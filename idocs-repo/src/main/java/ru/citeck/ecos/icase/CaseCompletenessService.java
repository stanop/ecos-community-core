package ru.citeck.ecos.icase;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

public interface CaseCompletenessService {

    /**
     * Get all completeness levels, available for the specified case.
     * 
     * @param caseNode
     * @return
     */
    Set<NodeRef> getAllLevels(NodeRef caseNode);
    
    /**
     * Get all completeness levels, that are completed for the specified case.
     * 
     * @param caseNode
     * @return
     */
    Set<NodeRef> getCompletedLevels(NodeRef caseNode);
    
    /**
     * Get requirement nodes for the specified level.
     * 
     * @param levelRef
     * @return
     */
    Set<NodeRef> getLevelRequirements(NodeRef levelRef);
    
    /**
     * Get passed requirements for the specified case.
     * 
     * @param caseNode
     * @return
     */
    Set<NodeRef> getPassedRequirements(NodeRef caseNode);
    
    /**
     * Get elements of the specified case that match specified requirement.
     * 
     * @param caseNode
     * @return
     */
    Set<NodeRef> getMatchedElements(NodeRef caseNode, NodeRef requirement);
    
    /**
     * Get current levels for the specified case.
     * 
     * @param caseNode
     * @return
     */
    Set<NodeRef> getCurrentLevels(NodeRef caseNode);

    /**
     * Re-calculate all case completeness levels of specified case.
     * 
     * @param caseNode
     */
    void recalculateLevels(NodeRef caseNode);

    /**
     * Re-calculate specified completeness level.
     * @param caseNode
     * @param levelRef
     */
    void recalculateLevel(NodeRef caseNode, NodeRef levelRef);
    
    /**
     * Re-calculate specified requirement
     * @param caseNode
     * @param requirement
     */
    void recalculateRequirement(NodeRef caseNode, NodeRef requirement);
    
}
