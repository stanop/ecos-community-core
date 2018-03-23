package ru.citeck.ecos.flowable.services.impl;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.flowable.app.domain.editor.Model;

/**
 * @author Roman Makarskiy
 */
public interface ModelMapper {
    @Insert({"INSERT INTO ACT_DE_MODEL (",
                "ID,",
                "NAME,",
                "MODEL_KEY,",
                "DESCRIPTION,",
                "MODEL_COMMENT,",
                "CREATED,",
                "CREATED_BY,",
                "LAST_UPDATED,",
                "LAST_UPDATED_BY,",
                "VERSION,",
                "MODEL_EDITOR_JSON,",
                "MODEL_TYPE)",
            "VALUES (",
                "#{id, jdbcType=VARCHAR},",
                "#{name, jdbcType=VARCHAR},",
                "#{key, jdbcType=VARCHAR},",
                "#{description, jdbcType=VARCHAR},",
                "#{comment, jdbcType=VARCHAR},",
                "#{created, jdbcType=TIMESTAMP},",
                "#{createdBy, jdbcType=VARCHAR},",
                "#{lastUpdated, jdbcType=TIMESTAMP},",
                "#{lastUpdatedBy, jdbcType=VARCHAR},",
                "#{version, jdbcType=INTEGER},",
                "#{modelEditorJson, jdbcType=VARCHAR},",
                "#{modelType, jdbcType=INTEGER}",
            ")"})
    void insertModel(Model model);

    @Select("SELECT TABLE_SCHEMA FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = 'ACT_DE_MODEL'")
    String getTableSchema();
}
