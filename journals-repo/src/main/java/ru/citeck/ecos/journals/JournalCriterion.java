package ru.citeck.ecos.journals;

import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.invariants.InvariantPriority;
import ru.citeck.ecos.invariants.InvariantScope;
import ru.citeck.ecos.invariants.xml.Invariant;
import ru.citeck.ecos.journals.xml.Criterion;
import ru.citeck.ecos.journals.xml.CriterionRegion;
import ru.citeck.ecos.search.SearchCriteriaSettingsRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JournalCriterion extends JournalViewElement {

    private List<InvariantDefinition> invariants = new ArrayList<>();
    private Map<String, JournalViewElement> regions = new HashMap<>();

    public JournalCriterion(QName attributeKey, Criterion criterion, String journalId,
                            NamespacePrefixResolver prefixResolver,
                            SearchCriteriaSettingsRegistry searchCriteriaSettingsRegistry) {
        super(criterion, journalId, searchCriteriaSettingsRegistry);

        if (criterion != null) {

            List<Invariant> xmlInvariants = criterion.getInvariant();

            if (xmlInvariants != null) {
                invariants = InvariantDefinition.Builder.buildInvariants(
                        attributeKey,
                        InvariantScope.AttributeScopeKind.PROPERTY, //doesn't matter
                        InvariantPriority.COMMON,
                        xmlInvariants,
                        prefixResolver
                );
            }

            List<CriterionRegion> xmlRegions = criterion.getRegion();

            if (xmlRegions != null) {
                for (CriterionRegion region : xmlRegions) {
                    regions.put(region.getName(), new JournalViewElement(region, journalId, searchCriteriaSettingsRegistry));
                }
            }
        }
    }

    public List<InvariantDefinition> getInvariants() {
        return invariants;
    }

    public Map<String, JournalViewElement> getRegions() {
        return regions;
    }
}
