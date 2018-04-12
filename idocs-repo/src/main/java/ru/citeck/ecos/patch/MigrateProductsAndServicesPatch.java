package ru.citeck.ecos.patch;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.behavior.idocs.ProductsAndServicesUtils;
import ru.citeck.ecos.model.ProductsAndServicesModel;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.*;

public class MigrateProductsAndServicesPatch extends AbstractPatch {

    private static final Log logger = LogFactory.getLog(MigrateProductsAndServicesPatch.class);
    private static final String PATCH_ID = "ru.citeck.ecos.patch.MigrateProductsAndServicesPatch";
    private static final String MSG_SUCCESS = PATCH_ID + ".success";
    private static final String MSG_NOT_REQUIRED = PATCH_ID + ".notRequired";
    private static final String pasFolderXPath = "/app:company_home/st:sites/cm:contracts/cm:dataLists/cm:products-and-services";

    @Autowired
    private ServiceRegistry serviceRegistry;
    @Autowired
    private CopyService copyService;

    private int countOfDocument = 0;
    private int countOfPas = 0;

    @Override
    protected String applyInternal() {
        logger.info("Starting execution of patch: " + I18NUtil.getMessage(PATCH_ID));

        NodeRef rootRef = serviceRegistry.getNodeService().getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        NodeRef pasFolderRef = getPasFolder(rootRef);

        if (pasFolderRef != null && nodeService.exists(pasFolderRef) && containerNotEmpty(pasFolderRef)) {
            migrateProductAndServices(pasFolderRef);
        } else {
            return I18NUtil.getMessage(MSG_NOT_REQUIRED);
        }

        logger.info("Finished executing of patch: " + I18NUtil.getMessage(PATCH_ID));
        return I18NUtil.getMessage(MSG_SUCCESS, countOfDocument, countOfPas);
    }

    private boolean containerNotEmpty(NodeRef pasFolderRef) {
        List<NodeRef> pas = RepoUtils.getChildrenByType(pasFolderRef, ProductsAndServicesModel.TYPE_ENTITY_COPIED,
                nodeService);
        return !pas.isEmpty();
    }

    private void migrateProductAndServices(NodeRef pasFolderRef) {
        List<NodeRef> productsAndServices = RepoUtils.getChildrenByType(pasFolderRef,
                ProductsAndServicesModel.TYPE_ENTITY_COPIED,
                nodeService);

        Map<NodeRef, Set<NodeRef>> documentToPas = getDocumentToPasRelation(productsAndServices);
        countOfDocument = documentToPas.size();
        countOfPas = productsAndServices.size();

        if (documentToPas.isEmpty()) {
            return;
        }

        StringBuilder msg = new StringBuilder();
        msg.append("\n").append("=====================================").append("\n");

        documentToPas.forEach((document, currentPas) -> {
            msg.append("Document: ").append(document).append("\n");

            currentPas.forEach(pas -> {
                NodeRef newPas = copyService.copy(pas, document,
                        ProductsAndServicesModel.ASSOC_CONTAINS_PRODUCTS_AND_SERVICES,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()));
                msg.append("     ").append(pas).append(" ----->").append(" ").append(newPas).append("\n");
            });

           /* ProductsAndServicesUtils.recalculateOrdersIfRequired(document,
                    ProductsAndServicesModel.ASSOC_CONTAINS_PRODUCTS_AND_SERVICES, nodeService);*/
        });

        msg.append("=====================================");
        logger.info(msg);
    }

    private Map<NodeRef, Set<NodeRef>> getDocumentToPasRelation(List<NodeRef> productAndServices) {
        Map<NodeRef, Set<NodeRef>> documentToPas = new HashMap<>();

        productAndServices.forEach(pas -> {
            List<NodeRef> sourceDocument = RepoUtils.getSourceNodeRefs(pas,
                    ProductsAndServicesModel.ASSOC_CONTAINS_PRODUCTS_AND_SERVICES, nodeService);

            sourceDocument.forEach(source -> {
                if (documentToPas.containsKey(source)) {
                    Set<NodeRef> currentPas = documentToPas.get(source);
                    currentPas.add(pas);
                    documentToPas.put(source, currentPas);
                } else {
                    documentToPas.put(source, new HashSet<>(Collections.singletonList(pas)));
                }
            });
        });

        return documentToPas;
    }

    private NodeRef getPasFolder(NodeRef rootRef) {
        List<NodeRef> refs = searchService.selectNodes(rootRef, pasFolderXPath, null,
                serviceRegistry.getNamespaceService(), false);

        if (refs.size() != 1) {
            return null;
        } else {
            return refs.get(0);
        }
    }
}
