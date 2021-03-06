package ru.citeck.ecos.records.models;

import lombok.Data;
import ru.citeck.ecos.records2.graphql.meta.annotation.MetaAtt;

import java.util.Date;

@Data
public class UserDTO {

    private String id;

    @MetaAtt("cm:userName")
    private String userName;

    @MetaAtt("cm:firstName")
    private String firstName;

    @MetaAtt("cm:lastName")
    private String lastName;

    @MetaAtt("cm:middleName")
    private String middleName;

    @MetaAtt("ecos:birthDate")
    private Date birthDate;

    @MetaAtt(".disp")
    private String displayName;

}
