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
 * 举报记录表。对应表 report_record。
 */
@Data
@TableName("report_record")
public class ReportRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 举报人 */
    private Long reporterId;

    /** 目标类型:1动态 2评论 3用户 */
    private Integer targetType;

    /** 目标 ID */
    private Long targetId;

    /** 举报原因类型 */
    private Integer reasonType;

    /** 补充说明 */
    private String reason;

    /** 状态:1待处理 2已处理 3驳回 */
    private Integer status;

    /** 处理结果 */
    private String handleResult;

    /** 处理人 */
    private Long handlerId;

    /** 处理时间 */
    private LocalDateTime handleTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
