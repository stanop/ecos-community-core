package ru.citeck.ecos.journals.invariants;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import ru.citeck.ecos.invariants.Feature;
import ru.citeck.ecos.invariants.InvariantConstants;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.journals.JournalType;

import java.util.ArrayList;
import java.util.List;

public class CriterionPropertyModelInvariantsProvider extends CriterionPropertyInvariantsProvider {

    @Override
    protected List<InvariantDefinition> getPropertyInvariants(JournalType journalType, PropertyDefinition propDef) {

        InvariantDefinition.Builder builder = new InvariantDefinition.Builder(namespaceService);
        builder.pushScope(propDef);

        List<InvariantDefinition> invariants = new ArrayList<>();

        for (ConstraintDefinition constraintDef : propDef.getConstraints()) {
            Constraint constraint = constraintDef.getConstraint();
            if (constraint instanceof ListOfValuesConstraint) {
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
            }
        }

        return invariants;
    }

}
