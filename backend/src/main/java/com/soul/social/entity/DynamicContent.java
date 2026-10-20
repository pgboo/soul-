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
 * 动态正文表（用户主页 dynamicCount 统计用）。
 */
@Data
@TableName("dynamic_content")
public class DynamicContent implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String content;

    /** 状态:1正常 2下架 */
    private Integer status;

    /** 审核:0已过审 1待审核 2拒绝 */
    private Integer auditStatus;

    /** 匿名:0否 1是 */
    private Integer anonymousFlag;

    /** 可见范围:1公开 2仅好友 3仅自己 */
    private Integer visibleScope;

    private Integer likeCount;

    private Integer commentCount;

    private Integer collectCount;

    private Integer reportCount;

    private String city;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
