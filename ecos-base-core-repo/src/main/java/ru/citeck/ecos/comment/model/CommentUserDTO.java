package ru.citeck.ecos.comment.model;

import lombok.Data;
import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.records2.graphql.meta.annotation.MetaAtt;

/**
 * @author Roman Makarskiy
 */
@Data
public class CommentUserDTO {

    private NodeRef id;

    @MetaAtt("cm:userName")
    private String userName;

    @MetaAtt("cm:firstName")
    private String firstName;

    @MetaAtt("cm:lastName")
    private String lastName;

    @MetaAtt("cm:middleName")
    private String middleName;

}
