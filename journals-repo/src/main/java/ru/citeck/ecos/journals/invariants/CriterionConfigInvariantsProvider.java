package ru.citeck.ecos.journals.invariants;

import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.journals.JournalCriterion;
import ru.citeck.ecos.journals.JournalType;

import java.util.List;

public class CriterionConfigInvariantsProvider extends CriterionInvariantsProvider {

    @Override
    protected void beforeInitImpl() {
        enableCache = false;
    }

    @Override
    public List<InvariantDefinition> getInvariantsImpl(JournalType journalType, QName typeName, QName attribute) {
        JournalCriterion criterion = journalType.getCriterion(attribute);
        return criterion.getInvariants();
    }
}
