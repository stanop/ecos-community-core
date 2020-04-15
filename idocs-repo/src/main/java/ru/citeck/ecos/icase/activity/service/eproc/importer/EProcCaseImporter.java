package ru.citeck.ecos.icase.activity.service.eproc.importer;

import com.hazelcast.util.ConcurrentHashSet;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
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
import ru.citeck.ecos.icase.activity.service.eproc.EProcActivityService;
import ru.citeck.ecos.icase.activity.service.eproc.importer.parser.CmmnSchemaParser;
import ru.citeck.ecos.icase.element.CaseElementService;
import ru.citeck.ecos.node.EcosTypeService;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records2.RecordRef;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

@Component
public class EProcCaseImporter {

    private EProcActivityService eprocActivityService;
    private CmmnSchemaParser cmmnSchemaParser;

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private AuthorityService authorityService;
    private CaseElementService caseElementService;
    private XmlContentDAO<Definitions> xmlContentDAO;
    private CMMNUtils utils;

    private EcosTypeService ecosTypeService;

    private Set<RecordRef> allowedEcosTypes = new ConcurrentHashSet<>();
    private Set<QName> allowedAlfTypes = new ConcurrentHashSet<>();

    @Autowired
    public EProcCaseImporter(EProcActivityService eprocActivityService,
                             CmmnSchemaParser cmmnSchemaParser,
                             NodeService nodeService,
                             DictionaryService dictionaryService,
                             AuthorityService authorityService,
                             CaseElementService caseElementService,
                             @Qualifier("caseTemplateContentDAO") XmlContentDAO<Definitions> xmlContentDAO,
                             CMMNUtils utils,
                             EcosTypeService ecosTypeService) {

        this.eprocActivityService = eprocActivityService;
        this.cmmnSchemaParser = cmmnSchemaParser;
        this.nodeService = nodeService;
        this.dictionaryService = dictionaryService;
        this.authorityService = authorityService;
        this.caseElementService = caseElementService;
        this.xmlContentDAO = xmlContentDAO;
        this.utils = utils;
        this.ecosTypeService = ecosTypeService;
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

    public void registerEcosType(RecordRef typeRef) {
        allowedEcosTypes.add(typeRef);
    }

    public void registerAlfrescoType(QName typeQName) {
        allowedAlfTypes.add(typeQName);
    }

    public boolean eprocCaseCreationAllowed(NodeRef caseRef) {
        RecordRef ecosType = ecosTypeService.getEcosType(caseRef);
        if (ecosType != null) {
            if (allowedEcosTypes.contains(ecosType)) {
                return true;
            }
        }

        QName alfCaseType = nodeService.getType(caseRef);
        return isAlfrescoTypeEnabled(alfCaseType);

    }

    private boolean isAlfrescoTypeEnabled(QName caseType) {
        ClassDefinition typeDef = dictionaryService.getClass(caseType);
        while (typeDef != null) {
            if (allowedAlfTypes.contains(typeDef.getName())) {
                return true;
            }
            typeDef = typeDef.getParentClassDefinition();
        }
        return false;
    }

}
