package ru.citeck.ecos.comment.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import lombok.Data;

import java.util.Date;

/**
 * @author Roman Makarskiy
 */
@Data
public class CommentDTO {

    private String id;
    private String text;

    private String record;

    private Date createdAt;
    private Date modifiedAt;

    private CommentUserDTO author;
    private CommentUserDTO editor;

    private boolean edited;

    private JsonNode permissions = NullNode.getInstance();

}
