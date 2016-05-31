/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.invariants.attr;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.dictionary.constraint.NumericRangeConstraint;
import org.alfresco.repo.dictionary.constraint.RegexConstraint;
import org.alfresco.repo.dictionary.constraint.StringLengthConstraint;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.citeck.ecos.invariants.AbstractInvariantAttributeType;
import ru.citeck.ecos.invariants.Feature;
import ru.citeck.ecos.invariants.InvariantConstants;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.invariants.InvariantPriority;
import ru.citeck.ecos.invariants.InvariantScope;
import ru.citeck.ecos.invariants.InvariantScope.AttributeScopeKind;
import ru.citeck.ecos.model.AttributeModel;

public class PropertiesAttributeType extends AbstractInvariantAttributeType {
    
    private static final Log logger = LogFactory.getLog(PropertiesAttributeType.class);

    private static final String NUMBER_MINMAX_CONSTRAINT_DESCRIPTION = "invariants.constraints.number-minmax.description";
    private static final String NUMBER_MIN_CONSTRAINT_DESCRIPTION = "invariants.constraints.number-min.description";
    private static final String NUMBER_MAX_CONSTRAINT_DESCRIPTION = "invariants.constraints.number-max.description";
    private static final String LENGTH_MINMAX_CONSTRAINT_DESCRIPTION = "invariants.constraints.length-minmax.description";
    private static final String LENGTH_MIN_CONSTRAINT_DESCRIPTION = "invariants.constraints.length-min.description";
    private static final String LENGTH_MAX_CONSTRAINT_DESCRIPTION = "invariants.constraints.length-max.description";
    private static final String REGEX_MATCH_CONSTRAINT_DESCRIPTION = "invariants.constraints.regex-match.description";
    private static final String REGEX_NOT_MATCH_CONSTRAINT_DESCRIPTION = "invariants.constraints.regex-not-match.description";
    
    @Override
    public QName getSupportedAttributeType() {
        return AttributeModel.TYPE_PROPERTY;
    }

    @Override
    public InvariantScope getAttributeScope(QName attributeName) {
        return new InvariantScope(attributeName, AttributeScopeKind.PROPERTY);
    }

    @Override
    public InvariantScope getAttributeTypeScope(QName attributeSubtype) {
        return new InvariantScope(attributeSubtype, AttributeScopeKind.PROPERTY_TYPE);
    }

    @Override
    public List<InvariantDefinition> getDefaultInvariants(QName attributeName, List<ClassDefinition> classes) {
        List<InvariantDefinition> invariants = new LinkedList<>();
        InvariantDefinition.Builder builder = new InvariantDefinition.Builder(prefixResolver);
        
        Set<PropertyDefinition> processedDefinitions = new HashSet<>(classes.size());
        for(ClassDefinition classDef : classes) {
            PropertyDefinition overridenProperty = dictionaryService.getProperty(classDef.getName(), attributeName);
            if(overridenProperty == null || processedDefinitions.contains(overridenProperty)) continue;
            processedDefinitions.add(overridenProperty);
            
            builder.pushScope(classDef)
                   .pushScope(overridenProperty)
                   .priority(InvariantPriority.COMMON);
            
            invariants.addAll(getDefaultInvariants(overridenProperty, builder, messageLookup));
            
            builder.popScope(overridenProperty);
            builder.popScope(classDef);
            
        }
        return invariants;
    }
    
    /*package*/ static List<InvariantDefinition> getDefaultInvariants(
            PropertyDefinition property, 
            InvariantDefinition.Builder builder, 
            MessageLookup messageLookup) {
        
        List<InvariantDefinition> invariants = new LinkedList<>();
        
        String title = property.getTitle(messageLookup);
        if(title != null) {
            invariants.add(builder.feature(Feature.TITLE).explicit(title).build());
        }
        
        String description = property.getDescription(messageLookup);
        if(description != null) {
            invariants.add(builder.feature(Feature.DESCRIPTION).explicit(description).build());
        }
        
        if(property.isProtected()) {
            invariants.add(builder.feature(Feature.PROTECTED).explicit(true).buildFinal());
        }
        
        if(property.isMandatoryEnforced()) {
            invariants.add(builder.feature(Feature.MANDATORY).explicit(true).buildFinal());
        } else if(property.isMandatory()) {
            invariants.add(builder.feature(Feature.MANDATORY).explicit(true).build());
        }
        
        if(property.isMultiValued()) {
            invariants.add(builder.feature(Feature.MULTIPLE).explicit(true).build());
        } else {
            invariants.add(builder.feature(Feature.MULTIPLE).explicit(false).buildFinal());
        }
        
        String defaultValue = property.getDefaultValue();
        if(defaultValue != null) {
            invariants.add(builder.feature(Feature.DEFAULT).explicit(defaultValue).build());
        }
        
        for(ConstraintDefinition constraintDef : property.getConstraints()) {
            Constraint constraint = constraintDef.getConstraint();
            if(constraint instanceof ListOfValuesConstraint) {
                ListOfValuesConstraint lovConstraint = (ListOfValuesConstraint) constraint;
                invariants.add(builder
                        .feature(Feature.OPTIONS)
                        .explicit(lovConstraint.getAllowedValues())
                        .build());
                invariants.add(builder
                        .feature(Feature.VALUE_TITLE)
                        .language(InvariantConstants.LANGUAGE_JAVASCRIPT)
                        .expression("(function() { var key = \"listconstraint." + 
                                lovConstraint.getShortName().replace(":", "_") + 
                                ".\" + value, msg = message(key); return msg != key ? msg : value; })()") 
                        .build());
            } else if(constraint instanceof NumericRangeConstraint) {
                NumericRangeConstraint nrConstraint = (NumericRangeConstraint) constraint;
                NumericRangeConstraint defaultConstraint = new NumericRangeConstraint();
                invariants.add(builder
                        .feature(Feature.VALID)
                        .description(chooseRangeMessage(
                                nrConstraint.getMinValue(), 
                                nrConstraint.getMaxValue(),
                                defaultConstraint.getMinValue(),
                                defaultConstraint.getMaxValue(),
                                NUMBER_MIN_CONSTRAINT_DESCRIPTION,
                                NUMBER_MAX_CONSTRAINT_DESCRIPTION,
                                NUMBER_MINMAX_CONSTRAINT_DESCRIPTION, messageLookup))
                        .language(InvariantConstants.LANGUAGE_JAVASCRIPT)
                        .expression(MessageFormat.format(
                                "value >= {0} && value <= {1}",
                                nrConstraint.getMinValue(),
                                nrConstraint.getMaxValue()
                        )).build());
            } else if(constraint instanceof StringLengthConstraint) {
                StringLengthConstraint slConstraint = (StringLengthConstraint) constraint;
                StringLengthConstraint defaultConstraint = new StringLengthConstraint();
                invariants.add(builder
                        .feature(Feature.VALID)
                        .description(chooseRangeMessage(
                                slConstraint.getMinLength(), 
                                slConstraint.getMaxLength(),
                                defaultConstraint.getMinLength(),
                                defaultConstraint.getMaxLength(),
                                LENGTH_MIN_CONSTRAINT_DESCRIPTION,
                                LENGTH_MAX_CONSTRAINT_DESCRIPTION,
                                LENGTH_MINMAX_CONSTRAINT_DESCRIPTION, messageLookup))
                        .language(InvariantConstants.LANGUAGE_JAVASCRIPT)
                        .expression(MessageFormat.format(
                                "(value+\"\").length >= {0} && (value+\"\").length <= {1}", 
                                slConstraint.getMinLength(),
                                slConstraint.getMaxLength()))
                        .build());
            } else if(constraint instanceof RegexConstraint) {
                RegexConstraint reConstraint = (RegexConstraint) constraint;
                invariants.add(builder
                        .feature(Feature.VALID)
                        .description(messageLookup.getMessage(
                                reConstraint.getRequiresMatch() 
                                        ? REGEX_MATCH_CONSTRAINT_DESCRIPTION
                                        : REGEX_NOT_MATCH_CONSTRAINT_DESCRIPTION, 
                                reConstraint.getExpression()))
                        .language(InvariantConstants.LANGUAGE_JAVASCRIPT)
                        .expression(MessageFormat.format(
                                "(value+\"\").match(/{0}/) ? {1} : {2}",
                                reConstraint.getExpression(),
                                reConstraint.getRequiresMatch(),
                                !reConstraint.getRequiresMatch()
                        )).build());
            } else {
                logger.info("Constraint class not supported, ignoring: " + constraint.getClass());
            }
        }
        return invariants;
    }
    
    private static String chooseRangeMessage(Object min, Object max,
            Object absoluteMin, Object absoluteMax, String minMessage,
            String maxMessage, String minmaxMessage, MessageLookup messageLookup) {
        if (min.equals(absoluteMin)) {
            return messageLookup.getMessage(maxMessage, max);
        } else if (min.equals(absoluteMax)) {
            return messageLookup.getMessage(minMessage, min);
        } else {
            return messageLookup.getMessage(minmaxMessage, min, max);
        }
    }

}
