package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public class RequirementModel {

    // model
    public static final String PREFIX = "req";
    
    public static final String NAMESPACE = "http://www.citeck.ru/model/case/requirement/1.0";
    
    public static final QName TYPE_COMPLETENESS_LEVEL = QName.createQName(NAMESPACE, "completenessLevel");
    public static final QName ASSOC_LEVEL_REQUIREMENT = QName.createQName(NAMESPACE, "levelRequirement");
    
    public static final QName TYPE_REQUIREMENT = QName.createQName(NAMESPACE, "requirement");
    public static final QName ASSOC_REQUIREMENT_SCOPE = QName.createQName(NAMESPACE, "requirementScope");
    
    public static final QName TYPE_MATCH = QName.createQName(NAMESPACE, "match");
    public static final QName PROP_MATCH_LEVEL = QName.createQName(NAMESPACE, "matchLevel");
    public static final QName PROP_MATCH_REQUIREMENT = QName.createQName(NAMESPACE, "matchRequirement");
    public static final QName PROP_MATCH_ELEMENT = QName.createQName(NAMESPACE, "matchElement");
    
    public static final QName TYPE_REQUIRED_LEVELS_PREDICATE = QName.createQName(NAMESPACE, "requiredLevelsPredicate");
    public static final QName PROP_LEVEL_REQUIRED = QName.createQName(NAMESPACE, "levelRequired");
    public static final QName ASSOC_REQUIRED_LEVELS = QName.createQName(NAMESPACE, "requiredLevels");
    
    public static final QName ASPECT_HAS_COMPLETENESS_LEVELS = QName.createQName(NAMESPACE, "hasCompletenessLevels");
    public static final QName ASSOC_COMPLETENESS_LEVELS = QName.createQName(NAMESPACE, "completenessLevels");
    public static final QName ASSOC_COMPLETED_LEVELS = QName.createQName(NAMESPACE, "completedLevels");
    public static final QName ASSOC_PASSED_REQUIREMENTS = QName.createQName(NAMESPACE, "passedRequirements");
    public static final QName ASSOC_MATCHES = QName.createQName(NAMESPACE, "matches");
    
}
