package com.soul.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.soul.social.entity.DynamicContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * dynamic_content 表 Mapper（主页动态数统计 + 后台含已删除查询）。
 */
@Mapper
public interface DynamicContentMapper extends BaseMapper<DynamicContent> {

    /**
     * 后台瞬间分页查询：<b>包含已逻辑删除的数据</b>。
     * <p>绕过 MyBatis-Plus 的逻辑删除自动过滤，显式返回 is_delete 字段供后台展示。
     *
     * @param page        分页对象
     * @param status      状态（可空）
     * @param auditStatus 审核状态（可空）
     * @param userId      作者 ID（可空）
     * @param keyword     正文关键字（可空）
     * @return 分页结果
     */
    @Select("<script>"
            + "SELECT id, user_id, content, status, audit_status, anonymous_flag, visible_scope, "
            + "like_count, comment_count, collect_count, report_count, city, "
            + "create_time, update_time, is_delete "
            + "FROM dynamic_content "
            + "<where>"
            + "  <if test='status != null'> AND status = #{status} </if>"
            + "  <if test='auditStatus != null'> AND audit_status = #{auditStatus} </if>"
            + "  <if test='userId != null'> AND user_id = #{userId} </if>"
            + "  <if test='keyword != null and keyword != \"\"'> AND content LIKE CONCAT('%', #{keyword}, '%') </if>"
            + "</where>"
            + "ORDER BY id DESC"
            + "</script>")
    IPage<DynamicContent> selectAdminPage(IPage<DynamicContent> page,
                                          @Param("status") Integer status,
                                          @Param("auditStatus") Integer auditStatus,
                                          @Param("userId") Long userId,
                                          @Param("keyword") String keyword);
}
