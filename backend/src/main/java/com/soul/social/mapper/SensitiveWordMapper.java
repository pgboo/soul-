package com.soul.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soul.social.entity.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * sensitive_word 表 Mapper。
 */
@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {

    /**
     * 按词精确统计**含已逻辑删除**的记录数（绕过 @TableLogic 自动追加的 is_delete=0）。
     * <p>用于新增/编辑前的重名校验：逻辑删除的旧行仍需计入，
     * 否则「删除后重新添加同一个词」会绕过应用层校验。
     *
     * @param word    词
     * @param excludeId 需排除的记录ID（编辑时排除自身），可为 null
     * @return 记录数
     */
    @Select("<script>"
            + "SELECT COUNT(*) FROM sensitive_word WHERE word = #{word}"
            + "<if test='excludeId != null'> AND id != #{excludeId}</if>"
            + "</script>")
    long countByWordIncludeDeleted(@Param("word") String word, @Param("excludeId") Long excludeId);
}
