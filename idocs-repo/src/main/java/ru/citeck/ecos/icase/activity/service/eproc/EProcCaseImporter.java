package ru.citeck.ecos.icase.activity.service.eproc;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.util.Pair;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.cmmn.CMMNUtils;
import ru.citeck.ecos.cmmn.model.Case;
import ru.citeck.ecos.cmmn.model.Definitions;
import ru.citeck.ecos.cmmn.service.util.CaseElementImport;
import ru.citeck.ecos.cmmn.service.util.CaseRolesImport;
import ru.citeck.ecos.content.dao.xml.XmlContentDAO;
import ru.citeck.ecos.icase.activity.dto.ProcessDefinition;
import ru.citeck.ecos.icase.activity.service.eproc.parser.CmmnSchemaParser;
import ru.citeck.ecos.icase.element.CaseElementService;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records2.RecordRef;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class EProcCaseImporter {

    private EProcActivityService eprocActivityService;
    private CmmnSchemaParser cmmnSchemaParser;

    private NodeService nodeService;
    private AuthorityService authorityService;
    private CaseElementService caseElementService;
    private XmlContentDAO<Definitions> xmlContentDAO;
    private CMMNUtils utils;

    @Autowired
    public EProcCaseImporter(EProcActivityService eprocActivityService,
                             CmmnSchemaParser cmmnSchemaParser,
                             NodeService nodeService,
                             AuthorityService authorityService,
                             CaseElementService caseElementService,
                             @Qualifier("caseTemplateContentDAO") XmlContentDAO<Definitions> xmlContentDAO,
                             CMMNUtils utils) {

        this.eprocActivityService = eprocActivityService;
        this.cmmnSchemaParser = cmmnSchemaParser;
        this.nodeService = nodeService;
        this.authorityService = authorityService;
        this.caseElementService = caseElementService;
        this.xmlContentDAO = xmlContentDAO;
        this.utils = utils;
    }

    public void importCase(RecordRef caseRef) {
        Pair<String, byte[]> revisionIdAndRawDefinition = getDefinitionBytes(caseRef);
        importDefinitionImpl(caseRef, revisionIdAndRawDefinition);
    }

    private void importDefinitionImpl(RecordRef caseRef, Pair<String, byte[]> revisionIdAndRawDefinition) {
        NodeRef caseNodeRef = RecordsUtils.toNodeRef(caseRef);

        String revisionId = revisionIdAndRawDefinition.getFirst();
        byte[] definitionBytes = revisionIdAndRawDefinition.getSecond();

        try (ByteArrayInputStream stream = new ByteArrayInputStream(definitionBytes)) {
            Definitions definitions = xmlContentDAO.read(stream);
            if (definitions == null || CollectionUtils.isEmpty(definitions.getCase())) {
                throw new RuntimeException("Definition is absent for caseRef=" + caseRef);
            }

            Case caseItem = definitions.getCase().get(0);

            CaseRolesImport caseRolesImport = new CaseRolesImport(nodeService, authorityService, utils);
            caseRolesImport.importRoles(caseNodeRef, caseItem.getCaseRoles());

            CaseElementImport caseElementImport = new CaseElementImport(caseElementService);
            caseElementImport.importCaseElementTypes(caseNodeRef, caseItem);

            ProcessDefinition processDefinition = cmmnSchemaParser.parse(caseItem);
            eprocActivityService.createDefaultState(caseRef, revisionId, processDefinition);
        } catch (IOException e) {
            throw new RuntimeException("Could not parse definition", e);
        }
    }

    private Pair<String, byte[]> getDefinitionBytes(RecordRef caseRef) {
        Pair<String, byte[]> revisionIdAndRawDefinition = eprocActivityService.getRawDefinitionForType(caseRef);
        if (revisionIdAndRawDefinition == null || revisionIdAndRawDefinition.getSecond() == null) {
            throw new RuntimeException("Can not find definition bytes for caseRef=" + caseRef);
        }
        return revisionIdAndRawDefinition;
    }

}
