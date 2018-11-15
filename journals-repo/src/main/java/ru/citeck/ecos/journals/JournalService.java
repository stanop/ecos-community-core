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
package ru.citeck.ecos.journals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.journals.invariants.CriterionInvariantsProvider;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.query.RecordsResult;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface JournalService {
    
    String JOURNALS_CONTAINER = "journals";

    void deployJournalTypes(InputStream inputStream);

    JournalType getJournalType(String id);

    JournalType needJournalType(String journalId);

    Optional<JournalType> getJournalForType(QName typeName);

    Collection<JournalType> getAllJournalTypes();

    void clearCache();

    List<InvariantDefinition> getCriterionInvariants(String journalId, QName attribute);

    void registerCriterionInvariantsProvider(CriterionInvariantsProvider provider);

    NodeRef getJournalRef(String id);

    RecordsResult<RecordRef> getRecords(String journalId,
                                        String query,
                                        String language,
                                        JGqlPageInfoInput pageInfo);

    RecordsResult<ObjectNode> getRecordsWithData(String journalId,
                                                 String query,
                                                 String language,
                                                 JGqlPageInfoInput pageInfo);
}
