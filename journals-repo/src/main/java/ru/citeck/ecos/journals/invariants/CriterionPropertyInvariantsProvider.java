package ru.citeck.ecos.journals.invariants;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.journals.JournalType;

import java.util.Collections;
import java.util.List;

public abstract class CriterionPropertyInvariantsProvider extends CriterionInvariantsProvider {

    @Override
    protected List<InvariantDefinition> getInvariantsImpl(JournalType journalType, QName typeName, QName attribute) {

        PropertyDefinition propDef = null;
        if (typeName != null) {
            propDef = dictionaryService.getProperty(typeName, attribute);
        }
        if (propDef == null) {
            propDef = dictionaryService.getProperty(attribute);
        }

        if (propDef == null) {
            return Collections.emptyList();
        }

        return getPropertyInvariants(journalType, propDef);
    }

    protected abstract List<InvariantDefinition> getPropertyInvariants(JournalType journalType, PropertyDefinition propDef);

}
