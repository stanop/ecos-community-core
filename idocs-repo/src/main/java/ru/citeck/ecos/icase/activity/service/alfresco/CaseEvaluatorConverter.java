package ru.citeck.ecos.icase.activity.service.alfresco;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.icase.CasePredicateUtils;
import ru.citeck.ecos.icase.evaluators.CompareLifecycleProcessVariableValueEvaluator;
import ru.citeck.ecos.icase.evaluators.ScriptEvaluator;
import ru.citeck.ecos.icase.evaluators.UserHasPermissionEvaluator;
import ru.citeck.ecos.icase.evaluators.UserInGroupEvaluator;
import ru.citeck.ecos.model.ConditionModel;
import ru.citeck.ecos.records2.evaluator.RecordEvaluatorDto;
import ru.citeck.ecos.records2.evaluator.evaluators.AlwaysFalseEvaluator;
import ru.citeck.ecos.records2.evaluator.evaluators.PredicateEvaluator;
import ru.citeck.ecos.records2.predicate.model.Predicate;
import ru.citeck.ecos.utils.EvaluatorUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class CaseEvaluatorConverter {

    private final Map<QName, Function<NodeRef, RecordEvaluatorDto>> extractorByTypeMapping;

    private NodeService nodeService;
    private NamespaceService namespaceService;

    @Autowired
    public CaseEvaluatorConverter(ServiceRegistry serviceRegistry) {
        this.nodeService = serviceRegistry.getNodeService();
        this.namespaceService = serviceRegistry.getNamespaceService();

        Map<QName, Function<NodeRef, RecordEvaluatorDto>> mapping = new HashMap<>();
        mapping.put(ConditionModel.CompareProcessVariable.TYPE, this::convertCompareProcessVariableEvaluator);
        mapping.put(ConditionModel.ComparePropertyValue.TYPE, this::convertComparePropertyValueEvaluator);
        mapping.put(ConditionModel.EvaluateScript.TYPE, this::convertScriptEvaluator);
        mapping.put(ConditionModel.UserInDocument.TYPE, this::convertUserInDocumentEvaluator);
        mapping.put(ConditionModel.UserInGroup.TYPE, this::convertUserInGroupEvaluator);
        mapping.put(ConditionModel.UserHasPermission.TYPE, this::convertUserHasPermissionEvaluator);

        this.extractorByTypeMapping = Collections.unmodifiableMap(mapping);
    }


    public RecordEvaluatorDto convertCondition(NodeRef conditionRef) {
        QName conditionType = nodeService.getType(conditionRef);
        Function<NodeRef, RecordEvaluatorDto> evaluatorExtractor = extractorByTypeMapping.get(conditionType);
        if (evaluatorExtractor == null) {
            throw new IllegalArgumentException("For condition " + conditionRef + " evaluator extractor not found");
        }

        RecordEvaluatorDto evaluatorDto = evaluatorExtractor.apply(conditionRef);
        if (evaluatorDto == null) {
            throw new IllegalStateException("For condition " + conditionRef + " extractor return null");
        }

        return evaluatorDto;
    }


    // Convert functions
    private RecordEvaluatorDto convertCompareProcessVariableEvaluator(NodeRef conditionRef) {
        Map<QName, Serializable> props = nodeService.getProperties(conditionRef);
        String variableName = (String) props.get(ConditionModel.CompareProcessVariable.PROP_VARIABLE);
        String expectedValue = (String) props.get(ConditionModel.CompareProcessVariable.PROP_VALUE);

        CompareLifecycleProcessVariableValueEvaluator.Config config =
                new CompareLifecycleProcessVariableValueEvaluator.Config();
        config.setVariableName(variableName);
        config.setVariableValue(expectedValue);

        return EvaluatorUtils.createEvaluatorDto(CompareLifecycleProcessVariableValueEvaluator.TYPE, config, false);
    }

    private RecordEvaluatorDto convertComparePropertyValueEvaluator(NodeRef conditionRef) {
        Map<QName, Serializable> props = nodeService.getProperties(conditionRef);

        QName property = (QName) props.get(ConditionModel.ComparePropertyValue.PROP_PROPERTY);
        String propertyName = property.toPrefixString(namespaceService);

        String rawOperation = (String) props.get(ConditionModel.ComparePropertyValue.PROP_OPERATION);
        CasePredicateUtils.Operation operation = CasePredicateUtils.Operation.valueOf(rawOperation);

        Serializable value = props.get(ConditionModel.ComparePropertyValue.PROP_VALUE);

        PredicateEvaluator.Config config = new PredicateEvaluator.Config();
        config.setPredicate(CasePredicateUtils.getSimplePredicate(propertyName, operation, value));

        return EvaluatorUtils.createEvaluatorDto(PredicateEvaluator.TYPE, config, false);
    }

    private RecordEvaluatorDto convertScriptEvaluator(NodeRef conditionRef) {
        String script = (String) nodeService.getProperty(conditionRef, ConditionModel.EvaluateScript.PROP_SCRIPT);

        ScriptEvaluator.Config config = new ScriptEvaluator.Config();
        config.setScript(script);

        return EvaluatorUtils.createEvaluatorDto(ScriptEvaluator.TYPE, config, false);
    }

    private RecordEvaluatorDto convertUserInDocumentEvaluator(NodeRef conditionRef) {
        Map<QName, Serializable> props = nodeService.getProperties(conditionRef);
        String rawProperty = (String) props.get(ConditionModel.UserInDocument.PROP_PROPERTY);
        if (StringUtils.isEmpty(rawProperty)) {
            return EvaluatorUtils.createEvaluatorDto(AlwaysFalseEvaluator.TYPE, null, false);
        }

        QName property = QName.resolveToQName(namespaceService, rawProperty);
        String propertyName = property.toPrefixString(namespaceService);

        String username = (String) props.get(ConditionModel.UserInDocument.PROP_USERNAME);
        if (StringUtils.isEmpty(username)) {
            username = AuthenticationUtil.getFullyAuthenticatedUser();
        }

        PredicateEvaluator.Config config = new PredicateEvaluator.Config();
        Predicate predicate = CasePredicateUtils.getSimplePredicate(propertyName,
                CasePredicateUtils.Operation.EQUALS, username);
        config.setPredicate(predicate);

        return EvaluatorUtils.createEvaluatorDto(PredicateEvaluator.TYPE, config, false);
    }

    private RecordEvaluatorDto convertUserInGroupEvaluator(NodeRef conditionRef) {
        Map<QName, Serializable> props = nodeService.getProperties(conditionRef);
        String userName = (String) props.get(ConditionModel.UserInGroup.PROP_USERNAME);
        String groupName = (String) props.get(ConditionModel.UserInGroup.PROP_GROUPNAME);

        UserInGroupEvaluator.Config config = new UserInGroupEvaluator.Config();
        config.setUserName(userName);
        config.setGroupName(groupName);

        return EvaluatorUtils.createEvaluatorDto(UserInGroupEvaluator.TYPE, config, false);
    }

    private RecordEvaluatorDto convertUserHasPermissionEvaluator(NodeRef conditionRef) {
        Map<QName, Serializable> props = nodeService.getProperties(conditionRef);
        String userName = (String) props.get(ConditionModel.UserHasPermission.PROP_USERNAME);
        String permission = (String) props.get(ConditionModel.UserHasPermission.PROP_PERMISSION);

        UserHasPermissionEvaluator.Config config = new UserHasPermissionEvaluator.Config();
        config.setUsername(userName);
        config.setPermission(permission);

        return EvaluatorUtils.createEvaluatorDto(UserHasPermissionEvaluator.TYPE, config, false);
    }

}
