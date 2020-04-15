package ru.citeck.ecos.icase.activity.service.eproc;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.CasePredicateUtils;
import ru.citeck.ecos.icase.activity.dto.EvaluatorDefinition;
import ru.citeck.ecos.icase.activity.dto.EvaluatorDefinitionData;
import ru.citeck.ecos.icase.activity.dto.EvaluatorDefinitionDataHolder;
import ru.citeck.ecos.icase.activity.service.eproc.importer.parser.CmmnDefinitionConstants;
import ru.citeck.ecos.icase.evaluators.*;
import ru.citeck.ecos.model.ConditionModel;
import ru.citeck.ecos.records2.evaluator.RecordEvaluatorDto;
import ru.citeck.ecos.records2.evaluator.evaluators.AlwaysFalseEvaluator;
import ru.citeck.ecos.records2.evaluator.evaluators.AlwaysTrueEvaluator;
import ru.citeck.ecos.records2.evaluator.evaluators.GroupEvaluator;
import ru.citeck.ecos.records2.evaluator.evaluators.PredicateEvaluator;
import ru.citeck.ecos.records2.predicate.model.Predicate;
import ru.citeck.ecos.utils.EvaluatorUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EProcCaseEvaluatorConverter {

    private final Map<String, EvaluatorConverter> converterByTypeMapping;

    private NamespaceService namespaceService;

    @Autowired
    public EProcCaseEvaluatorConverter(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;

        Map<String, EvaluatorConverter> mapping = new HashMap<>();
        mapping.put(ConditionModel.CompareProcessVariable.TYPE.getLocalName(), this::convertCompareProcessVariableEvaluator);
        mapping.put(ConditionModel.ComparePropertyValue.TYPE.getLocalName(), this::convertComparePropertyValueEvaluator);
        mapping.put(ConditionModel.EvaluateScript.TYPE.getLocalName(), this::convertScriptEvaluator);
        mapping.put(ConditionModel.UserInDocument.TYPE.getLocalName(), this::convertUserInDocumentEvaluator);
        mapping.put(ConditionModel.UserInGroup.TYPE.getLocalName(), this::convertUserInGroupEvaluator);
        mapping.put(ConditionModel.UserHasPermission.TYPE.getLocalName(), this::convertUserHasPermissionEvaluator);
        mapping.put(CmmnDefinitionConstants.COMPLETENESS_TYPE, this::convertCompletenessLevelsEvaluator);

        this.converterByTypeMapping = Collections.unmodifiableMap(mapping);
    }


    public RecordEvaluatorDto convertEvaluatorDefinition(EvaluatorDefinition definition) {
        if (definition == null || definition.getData() == null) {
            return null;
        }

        EvaluatorDefinitionDataHolder dataHolder = definition.getData().getAs(EvaluatorDefinitionDataHolder.class);
        if (dataHolder == null) {
            return null;
        }

        List<EvaluatorDefinitionData> definitionDataList = dataHolder.getData();
        if (CollectionUtils.isEmpty(definitionDataList)) {
            return null;
        } else if (definitionDataList.size() == 1) {
            RecordEvaluatorDto evaluatorDto = convertCondition(definitionDataList.get(0));
            setInverse(evaluatorDto, definition);
            return evaluatorDto;
        } else {
            GroupEvaluator.Config config = new GroupEvaluator.Config();
            config.setJoinBy(GroupEvaluator.JoinType.AND);
            List<RecordEvaluatorDto> groupedEvaluators = definitionDataList.stream()
                    .map(this::convertCondition)
                    .collect(Collectors.toList());
            config.setEvaluators(groupedEvaluators);
            RecordEvaluatorDto groupEvaluatorDto = EvaluatorUtils.createEvaluatorDto("group", config, false);
            setInverse(groupEvaluatorDto, definition);
            return groupEvaluatorDto;
        }
    }

    private void setInverse(RecordEvaluatorDto evaluatorDto, EvaluatorDefinition definition) {
        boolean inverse = BooleanUtils.toBoolean(definition.getInverse());
        evaluatorDto.setInverse(inverse);
    }

    private RecordEvaluatorDto convertCondition(EvaluatorDefinitionData evaluatorDefinitionData) {
        String conditionType = evaluatorDefinitionData.getType();
        EvaluatorConverter converter = converterByTypeMapping.get(conditionType);
        if (converter == null) {
            throw new RuntimeException("For condition " + evaluatorDefinitionData + " evaluator extractor not found");
        }

        RecordEvaluatorDto evaluatorDto = converter.convert(evaluatorDefinitionData);
        if (evaluatorDto == null) {
            throw new IllegalStateException("For condition " + evaluatorDefinitionData + " extractor return null");
        }

        return evaluatorDto;
    }


    // Convert functions
    private RecordEvaluatorDto convertCompareProcessVariableEvaluator(EvaluatorDefinitionData evaluatorDefinitionData) {
        Map<String, String> props = evaluatorDefinitionData.getAttributes();
        String variableName = props.get(ConditionModel.CompareProcessVariable.PROP_VARIABLE.getLocalName());
        String expectedValue = props.get(ConditionModel.CompareProcessVariable.PROP_VALUE.getLocalName());

        CompareLifecycleProcessVariableValueEvaluator.Config config =
                new CompareLifecycleProcessVariableValueEvaluator.Config();
        config.setVariableName(variableName);
        config.setVariableValue(expectedValue);

        return EvaluatorUtils.createEvaluatorDto(CompareLifecycleProcessVariableValueEvaluator.TYPE, config, false);
    }

    //TODO: supports only strings
    private RecordEvaluatorDto convertComparePropertyValueEvaluator(EvaluatorDefinitionData evaluatorDefinitionData) {
        Map<String, String> props = evaluatorDefinitionData.getAttributes();

        String rawProperty = props.get(ConditionModel.ComparePropertyValue.PROP_PROPERTY.getLocalName());
        QName property = QName.createQName(rawProperty, namespaceService);

        String rawOperation = props.get(ConditionModel.ComparePropertyValue.PROP_OPERATION.getLocalName());
        CasePredicateUtils.Operation operation = CasePredicateUtils.Operation.valueOf(rawOperation);

        String value = props.get(ConditionModel.ComparePropertyValue.PROP_VALUE.getLocalName());

        PredicateEvaluator.Config config = new PredicateEvaluator.Config();
        String propertyName = property.toPrefixString(namespaceService);
        config.setPredicate(CasePredicateUtils.getSimplePredicate(propertyName, operation, value));

        return EvaluatorUtils.createEvaluatorDto(PredicateEvaluator.TYPE, config, false);
    }

    private RecordEvaluatorDto convertScriptEvaluator(EvaluatorDefinitionData evaluatorDefinitionData) {
        Map<String, String> props = evaluatorDefinitionData.getAttributes();

        String script = props.get(ConditionModel.EvaluateScript.PROP_SCRIPT.getLocalName());

        ScriptEvaluator.Config config = new ScriptEvaluator.Config();
        config.setScript(script);

        return EvaluatorUtils.createEvaluatorDto(ScriptEvaluator.TYPE, config, false);
    }

    private RecordEvaluatorDto convertUserInDocumentEvaluator(EvaluatorDefinitionData evaluatorDefinitionData) {
        Map<String, String> props = evaluatorDefinitionData.getAttributes();

        String rawProperty = props.get(ConditionModel.UserInDocument.PROP_PROPERTY.getLocalName());
        if (StringUtils.isEmpty(rawProperty)) {
            return EvaluatorUtils.createEvaluatorDto(AlwaysFalseEvaluator.TYPE, null, false);
        }

        QName property = QName.createQName(rawProperty, namespaceService);
        String propertyName = property.toPrefixString(namespaceService);

        String username = props.get(ConditionModel.UserInDocument.PROP_USERNAME.getLocalName());
        if (StringUtils.isEmpty(username)) {
            username = AuthenticationUtil.getFullyAuthenticatedUser();
        }

        Predicate predicate = CasePredicateUtils.getSimplePredicate(propertyName,
                CasePredicateUtils.Operation.EQUALS, username);

        PredicateEvaluator.Config config = new PredicateEvaluator.Config();
        config.setPredicate(predicate);

        return EvaluatorUtils.createEvaluatorDto(PredicateEvaluator.TYPE, config, false);
    }

    private RecordEvaluatorDto convertUserInGroupEvaluator(EvaluatorDefinitionData evaluatorDefinitionData) {
        Map<String, String> props = evaluatorDefinitionData.getAttributes();

        String userName = props.get(ConditionModel.UserInGroup.PROP_USERNAME.getLocalName());
        String groupName = props.get(ConditionModel.UserInGroup.PROP_GROUPNAME.getLocalName());

        UserInGroupEvaluator.Config config = new UserInGroupEvaluator.Config();
        config.setUserName(userName);
        config.setGroupName(groupName);

        return EvaluatorUtils.createEvaluatorDto(UserInGroupEvaluator.TYPE, config, false);
    }

    private RecordEvaluatorDto convertUserHasPermissionEvaluator(EvaluatorDefinitionData evaluatorDefinitionData) {
        Map<String, String> props = evaluatorDefinitionData.getAttributes();

        String userName = props.get(ConditionModel.UserHasPermission.PROP_USERNAME.getLocalName());
        String permission = props.get(ConditionModel.UserHasPermission.PROP_PERMISSION.getLocalName());

        UserHasPermissionEvaluator.Config config = new UserHasPermissionEvaluator.Config();
        config.setUsername(userName);
        config.setPermission(permission);

        return EvaluatorUtils.createEvaluatorDto(UserHasPermissionEvaluator.TYPE, config, false);
    }

    private RecordEvaluatorDto convertCompletenessLevelsEvaluator(EvaluatorDefinitionData evaluatorDefinitionData) {
        Map<String, String> attributes = evaluatorDefinitionData.getAttributes();

        String rawCompletenessLevels = attributes.get(CmmnDefinitionConstants.COMPLETENESS_LEVELS_SET);
        if (StringUtils.isBlank(rawCompletenessLevels)) {
            log.warn("For completeness evaluatorDefinition has not levels. Def=" + evaluatorDefinitionData.toString());
            return EvaluatorUtils.createEvaluatorDto(AlwaysTrueEvaluator.TYPE, null, false);
        }

        CompletenessLevelsEvaluator.Config config = new CompletenessLevelsEvaluator.Config();
        String[] split = rawCompletenessLevels.split(",");
        config.setCompletenessLevelIdentifiers(Sets.newHashSet(split));

        return EvaluatorUtils.createEvaluatorDto(CompletenessLevelsEvaluator.TYPE, config, false);
    }


    private interface EvaluatorConverter {
        RecordEvaluatorDto convert(EvaluatorDefinitionData evaluatorDefinitionData);
    }

}
