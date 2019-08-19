package ru.citeck.ecos.comment;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.comment.model.CommentDTO;

/**
 * @author Roman Makarskiy
 */
public interface EcosCommentService {

    CommentDTO create(CommentDTO commentDTO);

    CommentDTO update(CommentDTO commentDTO);

    CommentDTO getById(String id);

    void delete(String id);

    PagingResults<NodeRef> listComments(NodeRef discussableNode, PagingRequest pagingRequest);

}
