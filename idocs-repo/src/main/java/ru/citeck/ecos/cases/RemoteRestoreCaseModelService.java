package ru.citeck.ecos.cases;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Remote restore case model service interface
 */
public interface RemoteRestoreCaseModelService {

    /**
     * Restore case model uuid
     */
    String RESTORE_CASE_MODEL_UUID = "remote-restore-case-model-";

    /**
     * Restore case models
     * @param documentRef Document reference
     */
    void restoreCaseModels(NodeRef documentRef);
}
