package ru.citeck.ecos.journals.invariants;

import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.journals.JournalCriterion;
import ru.citeck.ecos.journals.JournalType;

import java.util.List;

@Component
public class CriterionConfigInvariantsProvider extends CriterionInvariantsProvider {

    @Override
    protected void beforeInitImpl() {
        setEnableCache(false);
        setOrder(-1000);
    }

    @Override
    protected boolean isAttributeSupported(JournalType journalType, QName typeName, QName attribute) {
        return true;
    }

    @Override
    public List<InvariantDefinition> getInvariantsImpl(JournalType journalType, QName typeName, QName attribute) {
        JournalCriterion criterion = journalType.getCriterion(attribute);
        return criterion.getInvariants();
    }
}
