package ru.citeck.ecos.comment;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.forum.CommentServiceImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.comment.model.CommentDTO;

/**
 * Facade of alfresco {@link CommentServiceImpl} for working with {@link CommentDTO} in {@link CommentRecords}
 *
 * @author Roman Makarskiy
 */
@Service
public class EcosCommentServiceImpl implements EcosCommentService {

    private static final String WORKSPACE_PREFIX = "workspace://SpacesStore/";
    private static final boolean OFF_SUPPRESS_ROLL_UPS = false;

    private final CommentServiceImpl commentService;
    private final CommentFactory commentFactory;
    private final NodeService nodeService;

    @Autowired
    public EcosCommentServiceImpl(CommentServiceImpl commentService, CommentFactory commentFactory,
                                  NodeService nodeService) {
        this.commentService = commentService;
        this.commentFactory = commentFactory;
        this.nodeService = nodeService;
    }

    @Override
    public CommentDTO create(CommentDTO commentDTO) {
        String record = commentDTO.getRecord();

        if (StringUtils.isBlank(record)) {
            throw new IllegalArgumentException("Record id is mandatory parameter for creating comment");
        }

        NodeRef discussableRef = toNodeRef(record);
        NodeRef createdComment = commentService.createComment(discussableRef, "", commentDTO.getText(),
                OFF_SUPPRESS_ROLL_UPS);

        return commentFactory.fromNode(createdComment);
    }

    @Override
    public CommentDTO update(CommentDTO commentDTO) {
        String commentId = commentDTO.getId();

        if (StringUtils.isBlank(commentId)) {
            throw new IllegalArgumentException("Comment id is mandatory parameter for updating comment");
        }

        NodeRef commentRef = toNodeRef(commentId);
        commentService.updateComment(commentRef, "", commentDTO.getText());

        return commentFactory.fromNode(commentRef);
    }

    @Override
    public CommentDTO getById(String id) {
        NodeRef commentRef = toNodeRef(id);
        if (!nodeService.exists(commentRef)) {
            throw new IllegalArgumentException(String.format("Comment with id <%s> not found", id));
        }

        return commentFactory.fromNode(commentRef);
    }

    @Override
    public void delete(String id) {
        NodeRef commentRef = toNodeRef(id);
        commentService.deleteComment(commentRef);
    }

    @Override
    public PagingResults<NodeRef> listComments(NodeRef discussableNode, PagingRequest pagingRequest) {
        return commentService.listComments(discussableNode, pagingRequest);
    }

    private NodeRef toNodeRef(String recordId) {
        String ref = recordId;
        if (ref != null) {
            int idx = ref.lastIndexOf('@');
            if (idx > -1 && idx < ref.length() - 1) {
                ref = ref.substring(idx + 1);
            }
        }
        if (!StringUtils.contains(ref, WORKSPACE_PREFIX)) {
            ref = WORKSPACE_PREFIX + ref;
        }
        return new NodeRef(ref);
    }
}
