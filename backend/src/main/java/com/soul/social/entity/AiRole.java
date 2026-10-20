package com.soul.social.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * AI 角色表。对应表 ai_role。
 * <p>本期仅建表与后台 CRUD，不做真实 AI 调用。
 */
@Data
@TableName("ai_role")
public class AiRole implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** AI 角色名 */
    private String name;

    /** 头像 URL */
    private String avatar;

    /** 欢迎语 */
    private String welcomeText;

    /** 角色 Prompt */
    private String prompt;

    /** 状态:0禁用 1启用 */
    private Integer status;

    /** 排序（小在前） */
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
