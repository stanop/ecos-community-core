package ru.citeck.ecos.personal;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

/**
 * Service for users' personal documents management.
 */
public interface PersonalDocumentsService {

    /**
     * Get nodeRef of the 'personal documents' directory for user with 'userName'.
     * Create the 'personal documents' directory for user if it doesn't exist.
     *
     * @param userName - userName for 'personal documents' directory ensure
     * @return - 'personal documents' directory nodeRef
     */
    NodeRef ensureDirectory(String userName);

    /**
     * Get nodeRef of the temporary 'personal documents' directory.
     * Create the temporary 'personal documents' directory if it doesn't exist.
     *
     * @return
     */
    NodeRef ensureTempDirectory();

    /**
     * Get list of the personal documents for user with userName.
     *
     * @param userName
     * @return
     */
    List<NodeRef> getDocuments(String userName);

    /**
     * Move document to the personal folder. Do nothing if document is already in personal folder.
     *
     * @param document - document to move
     * @param userName
     */
    void ensureInPersonalFolder(NodeRef document, String userName);

}
