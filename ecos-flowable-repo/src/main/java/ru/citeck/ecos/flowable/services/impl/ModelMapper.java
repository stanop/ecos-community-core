package ru.citeck.ecos.flowable.services.impl;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.flowable.app.domain.editor.Model;
import org.flowable.app.domain.editor.ModelHistory;
import java.util.List;

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

    @Insert({"INSERT INTO ACT_DE_MODEL_HISTORY (",
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
            "MODEL_TYPE,",
            "MODEL_ID)",
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
            "#{modelType, jdbcType=INTEGER},",
            "#{modelId, jdbcType=VARCHAR}",
            ")"})
    void insertHistoryModel(ModelHistory model);

    @Select("SELECT TABLE_SCHEMA FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = 'ACT_DE_MODEL'")
    String getTableSchema();

    @Select("SELECT count(ID) FROM ACT_DE_MODEL WHERE MODEL_TYPE = 0 AND MODEL_KEY = #{modelKey}")
    Long getProcessModelsCountByModelKey(String modelKey);

    @Select("SELECT max(VERSION) FROM ACT_DE_MODEL WHERE MODEL_TYPE = 0 AND MODEL_KEY = #{modelKey}")
    Integer getLastProcessModelVersionByModelKey(String modelKey);

    @Select("SELECT ID as id, " +
            "NAME as name, " +
            "MODEL_KEY as key, " +
            "DESCRIPTION as description, " +
            "MODEL_COMMENT as comment, " +
            "CREATED as created, " +
            "CREATED_BY as createdBy, " +
            "LAST_UPDATED as lastUpdated, " +
            "LAST_UPDATED_BY as lastUpdatedBy, " +
            "VERSION as version, " +
            "MODEL_EDITOR_JSON as modelEditorJson," +
            "MODEL_TYPE as modelType " +
            "FROM ACT_DE_MODEL WHERE MODEL_TYPE = 0 AND MODEL_KEY = #{modelKey}")
    List<Model> getProcessModelsByModelKey(String modelKey);

    @Delete("DELETE FROM ACT_DE_MODEL WHERE ID = #{modelId}")
    void deleteProcessModelsById(String modelId);
}
